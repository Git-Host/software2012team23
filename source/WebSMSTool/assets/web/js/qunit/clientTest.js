  
module('WST-Objects');

test('Initialization', function() { 
	notEqual(wstLog, null, 'wstLog is instantiated');
	notEqual(wstAPI, null, 'wstAPI is instantiated');
	notEqual(wstTemplate, null, 'wstTemplate is instantiated');	
});


module('wstAPI');
test('settings', function() { 
	notEqual(wstAPI.options.api_url, '', 'API url is set');  
});




module('Async');
test('asynchronous test', function() {
    stop();  
    expect(1);  
    wstAPI.getContacts(function(data){
    	ok(data.state == 'success', 'Get Contacts successfully sent and received');
    });
    setTimeout(function(){start();}, 4000);  
});