import requests
from typing import Any

BASE_URL = "http://localhost:8080"
TIMEOUT = 5

USER = {"username": "user1", "password": "normalpassword"}
ADMIN = {"username": "admin", "password": "strongpassword"}

# extreme inputs

extreme_ids = [
    "George", -1, 0, 9999999999, None, 3.14, True, [], {}
]

extreme_strings = [
    "", " ", "a" * 5000, 123, -1, [], {}, "<script>alert(1)</script>"
]

def show(label: str, resp: Any):
    print(f"\n--- {label} ---")
    print(f"Status: {resp.status_code}")
    print(f"Body: {resp.text[:200]}")

    if resp.status_code >= 500:
        print(" ** SERVER ERROR **")
    elif resp.status_code >= 400:
        print(" ** CLIENT ERROR **")

def safe_text(value: Any) -> str:
    """Ensure we never send invalid request bodies"""
    try:
        return str(value)
    except Exception:
        return "<unserializable>"

def login(session: requests.Session, creds: dict) -> bool:
    resp = session.post(
        BASE_URL + "/login",
        data=creds,
        allow_redirects=False,
        timeout=TIMEOUT
    )
    print(f"Login attempt {creds['username']} → {resp.status_code}")
    return resp.status_code == 200

def fuzz_create_item(session: requests.Session, role: str):
    print(f"\n### FUZZ: POST /{role}")

    for name in extreme_strings:
        r = session.post(
            f"{BASE_URL}/{role}",
            data=safe_text(name),
            headers={"Content-Type": "text/plain"},
            timeout=TIMEOUT
        )
        show(f"name={name}", r)

def fuzz_get_items(session: requests.Session, role: str):
    print(f"\n### FUZZ: GET /{role}")

    r = session.get(
        f"{BASE_URL}/{role}",
        timeout=TIMEOUT
    )
    show("list items", r)

def fuzz_update_item(session: requests.Session, role: str):
    print(f"\n### FUZZ: PUT /{role}/{{id}}")

    for item_id in extreme_ids:
        r = session.put(
            f"{BASE_URL}/{role}/{safe_text(item_id)}",
            data="Updated item",
            headers={"Content-Type": "text/plain"},
            timeout=TIMEOUT
        )
        show(f"id={item_id}", r)

def fuzz_delete_item(session: requests.Session, role: str):
    print(f"\n### FUZZ: DELETE /{role}/{{id}}")

    for item_id in extreme_ids:
        r = session.delete(
            f"{BASE_URL}/{role}/{safe_text(item_id)}",
            timeout=TIMEOUT
        )
        show(f"id={item_id}", r)

def fuzz_logout(session: requests.Session):
    print("\n### FUZZ: POST /logout")

    r = session.post(
        BASE_URL + "/logout",
        timeout=TIMEOUT
    )
    show("logout", r)

def run():
    print("\n REST FUZZER")

    #user
    user_session = requests.Session()
    if login(user_session, USER):
        fuzz_create_item(user_session, "user")
        fuzz_get_items(user_session, "user")
        fuzz_update_item(user_session, "user")
        fuzz_delete_item(user_session, "user")
        fuzz_logout(user_session)

    #admin
    admin_session = requests.Session()
    if login(admin_session, ADMIN):
        fuzz_create_item(admin_session, "admin")
        fuzz_get_items(admin_session, "admin")
        fuzz_update_item(admin_session, "admin")
        fuzz_delete_item(admin_session, "admin")
        fuzz_logout(admin_session)

    print("\n Fuzzing Complete")

if __name__ == "__main__":
    run()
