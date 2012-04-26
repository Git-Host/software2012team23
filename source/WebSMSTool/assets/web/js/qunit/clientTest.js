  
module('WST-Object');

test('object test', function() { 
	notEqual(wstJS, null, 'wstJS is instantiated');
	ok(true, 'noch ein test');
});


test('settings', function() { 
	notEqual(wstJS.options.api_url, '', 'API url is set');  
});




module('Async');
test('asynchronous test', function() {
    stop();  
    expect(1);  
    
//    wstJS.pollInfo(function(state){
//    	ok(state, 'Poll info successfully sent and received');
//    });

    wstJS.getContacts(function(state){
    	ok(state, 'Get Contacts successfully sent and received');
    });
    

    
    
    setTimeout(function(){start();}, 2000);  
});