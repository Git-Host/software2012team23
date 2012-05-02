  
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

test('getContacts()', function() {
    stop();  
    expect(1);  
    wstAPI.getContacts(function(data){
    	ok(data.state == 'success', 'Get Contacts successfully sent and received');
    });
    setTimeout(function(){start();}, 4000);  
});




module('wstTemplate');
/* The handlebar templates are returned with an \n at the end */
test('get()', function() {
	var data = {title: 'Testtitel', firstName:'Stefan',lastName:'Lexow'};
	var html = wstTemplate.get('test', data);
	var cmp =  "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
	equal(html, cmp, 'Template "test" correctly fetched.');
});
