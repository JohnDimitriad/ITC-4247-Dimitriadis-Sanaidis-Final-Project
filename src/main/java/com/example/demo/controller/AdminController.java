package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ItemRepository itemRepository;

    public AdminController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public String getAll() {
        List<Item> items = itemRepository.findAll();
        if (items.isEmpty()) return "Admin: No items available.";
        return "Admin: Items -> " + items;
    }

    @PostMapping
    public ResponseEntity<String> addItem(@RequestBody String name, Authentication auth) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Item name must not be empty");
        }

        if (name.length() > 255) {
            return ResponseEntity
                    .badRequest()
                    .body("Item name too long");
        }

        Item item = new Item(name.trim());
        item.setOwner(auth.getName());
        itemRepository.save(item);

        return ResponseEntity.ok("Item '" + name + "' created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateItem(@PathVariable Long id,
                                             @RequestBody String name) {
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
        item.setName(name.trim());
        itemRepository.save(item);

        return ResponseEntity.ok("Updated item with id " + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    itemRepository.delete(item);
                    return ResponseEntity.ok("Deleted item '" + item.getName() + "'.");
                })
                .orElse(ResponseEntity.status(404).body("Item not found with id " + id + "."));
    }
}
