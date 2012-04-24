// Let's test this function  
function isEven(val) {  
    return val % 2 === 0;  
}  
  
module('Testmodule');

test('isEven()', function() { 
    ok(isEven(0), 'Zero is an even number'); 
    ok(isEven(2), 'So is two'); 
    ok(isEven(-4), 'So is negative four'); 
    ok(!isEven(1), 'One is not an even number'); 
    ok(!isEven(-7), 'Neither is negative seven');  
})  


module('JSON-Objects');
test('isEven()', function() { 
    ok(isEven(0), 'Zero is an even number'); 
    ok(isEven(2), 'So is two'); 
    ok(isEven(-4), 'So is negative four'); 
    ok(!isEven(1), 'One is not an even number'); 
    ok(!isEven(-7), 'Neither is negative seven');  
})  

module('API-Requests');
test('isEven()', function() { 
    ok(isEven(0), 'Zero is an even number'); 
    ok(isEven(2), 'So is two'); 
    ok(isEven(-4), 'So is negative four'); 
    ok(!isEven(1), 'One is not an even number'); 
    ok(!isEven(-7), 'Neither is negative seven');  
})  