var short_test_message = "This is an short message.";
var long_test_message = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
var message_address = "1234567890";

//we turn async to off, so our tests will run in order and we can predict the results. 
$.ajaxSetup({cache: false, async: false });



//$.mockjaxSettings.contentType = 'text/json';
//$.mockjax({
//	url : "api.html",
//	type: "post",
//    dataType: "json",	
//    data: '{"method":"info"}',
//    responseText: { state: "success" }
//});



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




setTimeout(function(){
	module('wstAPI');
	test('API - Simple Tests', function() {
	    stop();  
	    expect(3);  
	
		wstAPI.pollInfo(function(data){
			ok(data.state == 'success', 'PollInfo request is working.');
		});    
	    
	    wstAPI.getContacts(function(data){
	    	wstAPI.getContactsCallback(data);
	    	ok(data.state == 'success', 'Get Contacts successfully received');
	    });
	
	    
	    wstAPI.sendSMSMessage(message_address,short_test_message, function(data){
	    	ok(data.state == 'success', 'SMS successfully sent to phone application.');
	    }); 
	    setTimeout(function(){start();}, 1000);  
	});
},2000);





setTimeout(function(){
	module('wstAPI');
test('API - Test sms sent notficiation', function(){
	stop();
	expect(4);
	
	wstAPI.pollInfo(function(data){
		ok(data.sms_sent_success == true, 'SMS sent success returned true.');
		ok(data.sms_sent_success_messages.length > 0, 'SMS success messages are set.');
		var msg = data.sms_sent_success_messages[0];
		equal(msg.body,short_test_message, 'SMS sent body is eqal to webapp body sent.');
		equal(msg.address,message_address, 'SMS sent address is eqal to webapp address sent.');
	});
    setTimeout(function(){start();}, 1000);  
});
},3000);




setTimeout(function(){
	module('wstAPI');
test('API - Test long text sms', function() {
    stop();   
    expect(1);
    
    wstAPI.sendSMSMessage(message_address,long_test_message, function(data){
    	ok(data.state == 'success', 'Long SMS successfully sent to phone application.');
    }); 
	
    setTimeout(function(){start();}, 1000);
    
});
},5000);


setTimeout(function(){
	module('wstAPI');
	test('API - Test long text sms sent notification', function() {
	    stop();   
	    expect(4);
    
	    
		wstAPI.pollInfo(function(data){
				ok(data.sms_sent_success == true, 'SMS sent success returned true.');
				ok(data.sms_sent_success_messages.length > 0, 'SMS success messages are set.');
				var msg = data.sms_sent_success_messages[0];
				equal(msg.body,long_test_message, 'SMS sent body is eqal to webapp body sent.');
				equal(msg.address,message_address, 'SMS sent address is eqal to webapp address sent.');
		});
		
	    setTimeout(function(){start();}, 1000);
	    
	});
},7000);




module('wstTemplate');
/* The handlebar templates are returned with an \n at the end */
test('get()', function() {
	var data = {title: 'Testtitel', firstName:'Stefan',lastName:'Lexow'};
	var html = wstTemplate.get('test', data);
	var cmp =  "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
	equal(html, cmp, 'Template "test" correctly fetched.');
});
