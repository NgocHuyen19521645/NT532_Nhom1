const iotModel=require('../model/iot.model');

var number = 0;

function create(temp, gas) {
  return new Promise((resolve, reject) => {
    
    if(temp > 30 & gas > 50)
    {
      number = 1;
    }

    var message = 'Nhom XXXX !!!!';
    console.log(message);
    console.log('count: ' + number);
    console.log('END !!!!!!!!!');
    resolve({
      error: false,
      message: 'Ngoc Huyen !!!!!!!!!',
      data : {
        'temp': temp,
        'count': number,
        'gas': gas,
      }
    })
    
  })
}

function get() {
  return new Promise((resolve, reject) => {

    resolve({
      error: false,
      message: 'Ngoc Huyen !!!!!!!!!',
      data : {
        'count': number,
      }
    })
 
  });
}

module.exports = {
  create : create,
  get : get
}
