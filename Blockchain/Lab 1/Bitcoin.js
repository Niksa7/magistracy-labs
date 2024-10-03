// See https://www.blockcypher.com/dev/bitcoin/#address-balance-endpoint for documentation

const https = require('https')
const options = {
  hostname: 'api.blockcypher.com',
  port: 443,
  path: '/v1/btc/test3/addrs/tb1q45d3c58ahr9leutyrhcvnjzmjts5eehphyvd9fxp46z9nagk99kq30ydgg/balance',
  method: 'GET'
}

const req = https.request(options, res => {
  console.log(`statusCode: ${res.statusCode}`)

  res.on('data', d => {
    let b = JSON.parse(d);
    let balance = b.balance / 100000000;
    console.log(`balance = ${balance}`);
  })
})

req.on('error', error => {
  console.error(error)
})

req.end()

 
