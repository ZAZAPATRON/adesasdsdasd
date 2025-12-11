// Verification codes stored with timestamp (expires in 10 minutes)
const CODE_EXPIRY = 10 * 60 * 1000; // 10 minutes
let verificationCodes = JSON.parse(localStorage.getItem('fpshud_codes')) || {};

// Generate 6-digit code
function generateCode() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// Send verification code via fake email (in real app, would call backend)
function sendVerificationCode() {
  const email = document.getElementById('email-input').value.trim();
  const msgEl = document.getElementById('email-message');
  
  if (!email) {
    msgEl.textContent = 'Email adresini gir!';
    msgEl.style.color = '#d32f2f';
    return;
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    msgEl.textContent = 'Geçerli bir email gir!';
    msgEl.style.color = '#d32f2f';
    return;
  }

  // Generate code and store it
  const code = generateCode();
  verificationCodes[email] = {
    code: code,
    timestamp: Date.now()
  };
  localStorage.setItem('fpshud_codes', JSON.stringify(verificationCodes));

  // Show success message
  msgEl.textContent = `✓ Kod ${email} adresine gönderildi`;
  msgEl.style.color = '#4ade80';

  // Log code to console for testing (in real app, would be sent via email)
  console.log(`📧 Verification code for ${email}: ${code}`);

  // Switch to code input step
  setTimeout(() => {
    document.getElementById('email-step').classList.remove('active');
    document.getElementById('code-step').classList.add('active');
    document.getElementById('code-input').focus();
  }, 1000);
}

// Verify entered code
function verifyCode() {
  const email = document.getElementById('email-input').value.trim();
  const enteredCode = document.getElementById('code-input').value.trim();
  const msgEl = document.getElementById('code-message');

  if (!enteredCode) {
    msgEl.textContent = 'Kodu gir!';
    msgEl.style.color = '#d32f2f';
    return;
  }

  const storedData = verificationCodes[email];
  
  // Check if code exists and hasn't expired
  if (!storedData) {
    msgEl.textContent = 'Email adresine ait kod bulunamadı. Tekrar deneyin.';
    msgEl.style.color = '#d32f2f';
    return;
  }

  if (Date.now() - storedData.timestamp > CODE_EXPIRY) {
    msgEl.textContent = 'Kod süresi doldu. Tekrar deneyin.';
    msgEl.style.color = '#d32f2f';
    delete verificationCodes[email];
    localStorage.setItem('fpshud_codes', JSON.stringify(verificationCodes));
    return;
  }

  if (enteredCode !== storedData.code) {
    msgEl.textContent = 'Kod yanlış. Tekrar deneyin.';
    msgEl.style.color = '#d32f2f';
    return;
  }

  // Success! Mark as registered
  localStorage.setItem('fpshud_registered', JSON.stringify({
    email: email,
    registeredAt: new Date().toISOString()
  }));

  // Clean up code
  delete verificationCodes[email];
  localStorage.setItem('fpshud_codes', JSON.stringify(verificationCodes));

  // Show success step
  document.getElementById('code-step').classList.remove('active');
  document.getElementById('success-step').classList.add('active');
  document.getElementById('success-email').textContent = `Email: ${email}`;

  // Close modal after 3 seconds
  setTimeout(() => {
    closeAuthModal();
  }, 3000);
}

// Go back to email input
function backToEmail() {
  document.getElementById('code-step').classList.remove('active');
  document.getElementById('email-step').classList.add('active');
  document.getElementById('code-input').value = '';
  document.getElementById('code-message').textContent = '';
  document.getElementById('email-input').focus();
}

// Close auth modal
function closeAuthModal() {
  document.getElementById('auth-modal').style.display = 'none';
}

// Allow Enter key in inputs
document.addEventListener('DOMContentLoaded', function() {
  document.getElementById('email-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') sendVerificationCode();
  });
  
  document.getElementById('code-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') verifyCode();
  });
});
