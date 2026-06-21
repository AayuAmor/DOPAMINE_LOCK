# Firebase Security Notes

Recommended Realtime Database rules for user profiles:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

Recommended Realtime Database rules for focus sessions:

```json
{
  "rules": {
    "focusSessions": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

Keep Firebase client config and local secrets out of git. If `app/google-services.json`
has ever been pushed to a public repository, remove it from tracking with
`git rm --cached app/google-services.json` and rotate the Firebase project keys.
