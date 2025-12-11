async function loadConfig(){
  try{
    const r = await fetch('./config.json');
    return await r.json();
  }catch(e){
    return {paymentApiBase: null};
  }
}

async function createPayment(method, amount){
  const cfg = await loadConfig();
  const base = cfg.paymentApiBase;
  if(!base || base.includes('your-payment-server')){
    alert('Ödeme API adresi yapılandırılmamış. payment-server çalıştırmanız veya config.json güncellemeniz gerekir.');
    return;
  }

  const resp = await fetch(base.replace(/\/$/, '') + '/create-payment', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({method, amount, returnUrl: window.location.origin + '/thankyou.html'})
  });

  if(!resp.ok){
    const t = await resp.text();
    alert('Ödeme oluşturulamadı: ' + t);
    return;
  }

  const data = await resp.json();
  if(data.paymentUrl){
    window.location.href = data.paymentUrl;
  }else if(data.url){
    window.location.href = data.url;
  }else{
    alert('Beklenmeyen cevap: ' + JSON.stringify(data));
  }
}

document.addEventListener('DOMContentLoaded', ()=>{
  const pap = document.getElementById('buy-papara');
  const troy = document.getElementById('buy-troy');
  const ziraat = document.getElementById('buy-ziraat');
  if(pap) pap.addEventListener('click', ()=>createPayment('papara', 5.0));
  if(troy) troy.addEventListener('click', ()=>createPayment('card', 5.0));
  if(ziraat) ziraat.addEventListener('click', ()=>createPayment('bank', 5.0));
});
