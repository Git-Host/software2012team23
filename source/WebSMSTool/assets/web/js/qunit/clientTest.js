  
module('WST-Object');

test('object test', function() { 
	notEqual(wstAPI, null, 'wstAPI is instantiated');
	ok(true, 'noch ein test');
});


test('settings', function() { 
	notEqual(wstAPI.options.api_url, '', 'API url is set');  
});




module('Async');
test('asynchronous test', function() {
    stop();  
    expect(1);  
    
//    wstJS.pollInfo(function(state){
//    	ok(state, 'Poll info successfully sent and received');
//    });

    wstAPI.getContacts(function(data){
    	ok(data.state == 'success', 'Get Contacts successfully sent and received');
    });
    
    setTimeout(function(){start();}, 2000);  
});