  
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

test('API - Test', function() {
    stop();  
    expect(1);  
//    wstAPI.getContacts(function(data){
//    	wstAPI.getContactsCallback(data);
//    	ok(data.state == 'success', 'Get Contacts successfully received');
//    });
    
    wstAPI.sendSMSMessage("1234567890","This is an short message.", function(data){
    	ok(data.state == 'success', 'SMS successfully sent to phone application.');
    }); 
    
//    wstAPI.pollInfo(function(data){
//    	ok(data.state == 'success', 'PollInfo request is working.');
//    });
    
    setTimeout(function(){start();}, 2000);  
});


module('wstTemplate');
/* The handlebar templates are returned with an \n at the end */
test('get()', function() {
	var data = {title: 'Testtitel', firstName:'Stefan',lastName:'Lexow'};
	var html = wstTemplate.get('test', data);
	var cmp =  "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
	equal(html, cmp, 'Template "test" correctly fetched.');
});
