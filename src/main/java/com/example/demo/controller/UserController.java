package com.example.demo.controller;

import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final ItemRepository itemRepository;

    public UserController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public ResponseEntity<String> getAll(Authentication auth) {
        List<Item> items = itemRepository.findByOwner(auth.getName());
        if (items.isEmpty()) return ResponseEntity.ok("No items available for your account.");
        return ResponseEntity.ok("Items -> " + items);
    }

    @PostMapping
    public ResponseEntity<String> addItem(@RequestBody String name, Authentication auth) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Item name must not be empty");
        }
        if (name.length() > 255) {
            return ResponseEntity.badRequest().body("Item name too long");
        }

        Item item = new Item(name.trim());
        item.setOwner(auth.getName());
        itemRepository.save(item);

        return ResponseEntity.ok("Item '" + name + "' created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateItem(@PathVariable Long id,
                                             @RequestBody String name,
                                             Authentication auth) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Item name must not be empty");
        }
        if (name.length() > 255) {
            return ResponseEntity.badRequest().body("Item name too long");
        }

        Optional<Item> opt = itemRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Item not found with id " + id);
        }

        Item item = opt.get();
        if (!item.getOwner().equals(auth.getName())) {
            return ResponseEntity.status(403).body("Access denied: Not your item.");
        }

        item.setName(name.trim());
        itemRepository.save(item);

        return ResponseEntity.ok("Updated item with id " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id, Authentication auth) {
        return itemRepository.findById(id)
                .map(item -> {
                    if (!item.getOwner().equals(auth.getName())) {
                        return ResponseEntity.status(403).body("Access denied: Not your item.");
                    }
                    itemRepository.delete(item);
                    return ResponseEntity.ok("Deleted item '" + item.getName() + "'.");
                })
                .orElse(ResponseEntity.status(404).body("Item not found with id " + id + "."));
    }
}
