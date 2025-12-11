FPS HUD — Payment example

This is a small demo Node.js server that simulates a payment provider checkout and shows how you
could integrate a provider webhook to confirm payments.

Usage:

1. Install dependencies:

```bash
npm install
```

2. Start server:

```bash
npm start
```

3. Create a fake payment (example using `curl`):

```bash
curl -X POST http://localhost:3000/create-payment -H "Content-Type: application/json" -d '{"method":"papara","amount":5.0,"returnUrl":"https://your-site.example/thankyou"}'
```

4. The response contains a `paymentUrl` — opening it simulates a successful payment and redirects to `returnUrl` with `paymentId` and `status` query params.

5. Configure your provider to call `/webhook` when payments complete; verify signatures on production.

Security:
- Never trust webhook requests without verification (provider signature).
- Use HTTPS in production.
