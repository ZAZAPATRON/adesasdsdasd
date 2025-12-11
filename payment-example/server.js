const express = require('express');
const bodyParser = require('body-parser');
const app = express();
app.use(bodyParser.json());

// Simple in-memory store for demo
const payments = new Map();

// Create a fake payment and return a redirect URL (simulate provider)
app.post('/create-payment', (req, res) => {
  const { method, amount, returnUrl } = req.body || {};
  if (!method || !amount || !returnUrl) return res.status(400).json({ error: 'method, amount, returnUrl required' });
  const id = 'pay_' + Math.random().toString(36).slice(2,10);
  payments.set(id, { method, amount, status: 'pending', returnUrl });
  // paymentUrl would be provider checkout; here we simulate by returning a local simulate URL
  const paymentUrl = `${req.protocol}://${req.get('host')}/simulate/${id}`;
  res.json({ id, paymentUrl });
});

// Simulate user completing payment at provider and redirect back
app.get('/simulate/:id', (req, res) => {
  const id = req.params.id;
  const p = payments.get(id);
  if (!p) return res.status(404).send('Payment not found');
  p.status = 'paid';
  // Optionally call webhook internally (demo)
  // redirect back to returnUrl with success param
  const redirect = new URL(p.returnUrl);
  redirect.searchParams.set('paymentId', id);
  redirect.searchParams.set('status', 'success');
  res.redirect(redirect.toString());
});

// Webhook endpoint to receive provider notifications
app.post('/webhook', (req, res) => {
  console.log('Webhook received:', req.body);
  // In real setup, verify signature, update order status, etc.
  res.json({ ok: true });
});

app.get('/', (req, res) => {
  res.send('FPS HUD payment example. POST /create-payment with JSON {method,amount,returnUrl}');
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log('Payment example listening on', port));
