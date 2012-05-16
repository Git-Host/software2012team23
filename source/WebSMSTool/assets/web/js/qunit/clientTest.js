var short_test_message = "This is an short message.";
var long_test_message = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
var message_address = "1234567890";

var contact_test_image = "R0lGODlhBQAFAPcAAAAAAAAAMwAAZgAAmQAAzAAA/wArAAArMwArZgArmQArzAAr/wBVAABVMwBV"+
"ZgBVmQBVzABV/wCAAACAMwCAZgCAmQCAzACA/wCqAACqMwCqZgCqmQCqzACq/wDVAADVMwDVZgDV"+
"mQDVzADV/wD/AAD/MwD/ZgD/mQD/zAD//zMAADMAMzMAZjMAmTMAzDMA/zMrADMrMzMrZjMrmTMr"+
"zDMr/zNVADNVMzNVZjNVmTNVzDNV/zOAADOAMzOAZjOAmTOAzDOA/zOqADOqMzOqZjOqmTOqzDOq"+
"/zPVADPVMzPVZjPVmTPVzDPV/zP/ADP/MzP/ZjP/mTP/zDP//2YAAGYAM2YAZmYAmWYAzGYA/2Yr"+
"AGYrM2YrZmYrmWYrzGYr/2ZVAGZVM2ZVZmZVmWZVzGZV/2aAAGaAM2aAZmaAmWaAzGaA/2aqAGaq"+
"M2aqZmaqmWaqzGaq/2bVAGbVM2bVZmbVmWbVzGbV/2b/AGb/M2b/Zmb/mWb/zGb//5kAAJkAM5kA"+
"ZpkAmZkAzJkA/5krAJkrM5krZpkrmZkrzJkr/5lVAJlVM5lVZplVmZlVzJlV/5mAAJmAM5mAZpmA"+
"mZmAzJmA/5mqAJmqM5mqZpmqmZmqzJmq/5nVAJnVM5nVZpnVmZnVzJnV/5n/AJn/M5n/Zpn/mZn/"+
"zJn//8wAAMwAM8wAZswAmcwAzMwA/8wrAMwrM8wrZswrmcwrzMwr/8xVAMxVM8xVZsxVmcxVzMxV"+
"/8yAAMyAM8yAZsyAmcyAzMyA/8yqAMyqM8yqZsyqmcyqzMyq/8zVAMzVM8zVZszVmczVzMzV/8z/"+
"AMz/M8z/Zsz/mcz/zMz///8AAP8AM/8AZv8Amf8AzP8A//8rAP8rM/8rZv8rmf8rzP8r//9VAP9V"+
"M/9VZv9Vmf9VzP9V//+AAP+AM/+AZv+Amf+AzP+A//+qAP+qM/+qZv+qmf+qzP+q///VAP/VM//V"+
"Zv/Vmf/VzP/V////AP//M///Zv//mf//zP///wAAAAAAAAAAAAAAACH5BAEAAPwALAAAAAAFAAUA"+
"AAgYAKW9moZtWrZprrK9SohwGsKD2AZGnBYQADs=";

var contact_phone_numbers = new Array();
contact_phone_numbers[0] = new Object();
contact_phone_numbers[0]["type"] = "1";
contact_phone_numbers[0]["number"] = "1234567890";
contact_phone_numbers[1] = new Object();
contact_phone_numbers[1]["type"] = "2";
contact_phone_numbers[1]["number"] = "9876543210";

var contact_test_entry = {
		display_name: "Display Name",
		last_name: "Lastname",
		name: "Name",
		id: "1234",
		image: contact_test_image,
		phone_numbers : contact_phone_numbers
}


var contact_tempalte_result ="<div class=\"list_entry\">\n"+
"<img src=\""+contact_test_image+"\" alt=\"Name Lastname\" />\n"+
"<h4>Name Lastname</h4>\n"+
"<p>1234567890</p><p>9876543210</p>\n"+
"</div>\n";




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
	    expect(4);  
	
		wstAPI.pollInfo(function(data){
			ok(data.state == 'success', 'PollInfo request is working.');
		});    
	    
	    wstAPI.getContacts(function(data){
	    	ok(data.state == 'success', 'Get Contacts successfully received');
	    });
	    
	    wstAPI.sendSMSMessage(message_address,short_test_message, function(data){
	    	ok(data.state == 'success', 'SMS successfully sent to phone application.');
	    }); 
	    
	    wstAPI.fetchSMSThread(1, function(data){
	    	ok(data.state == 'success', 'SMS Thread successfully fetched.');
	    });
	    
	    
	    setTimeout(function(){start();}, 1000);  
	});

	
	test('API - Fetch sms thread delivers non "null" person', function() {
	    stop();  
	    expect(2);  
		    
	    wstAPI.fetchSMSThread(1, function(data){
	    	ok(data.state == 'success', 'SMS Thread successfully fetched.');
	    	
	    	var success = true;
	    	for(var i = 0; i < data.thread_messages.length; i++){
	    		if(data.thread_messages[i].person == "null"){
	    			success = false;
	    			break;
	    		}
	    	}
	    	ok(success === true, 'No SMS Messages without person attribute == null found.');
	    });
	    
	    
	    setTimeout(function(){start();}, 1000);  
	});
	
	
	
},3000);





//setTimeout(function(){
//	module('wstAPI');
//test('API - Test sms sent notficiation', function(){
//	stop();
//	expect(4);
//	
//	wstAPI.pollInfo(function(data){
//		ok(data.sms_sent_success == true, 'SMS sent success returned true.');
//		ok(data.sms_sent_success_messages.length > 0, 'SMS success messages are set.');
//		var msg = data.sms_sent_success_messages[0];
//		equal(msg.body,short_test_message, 'SMS sent body is eqal to webapp body sent.');
//		equal(msg.address,message_address, 'SMS sent address is eqal to webapp address sent.');
//	});
//    setTimeout(function(){start();}, 1000);  
//});
//},4000);




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
},6000);


//setTimeout(function(){
//	module('wstAPI');
//	test('API - Test long text sms sent notification', function() {
//	    stop();   
//	    expect(4);
//    
//	    
//		wstAPI.pollInfo(function(data){
//				ok(data.sms_sent_success == true, 'SMS sent success returned true.');
//				ok(data.sms_sent_success_messages.length > 0, 'SMS success messages are set.');
//				var msg = data.sms_sent_success_messages[0];
//				equal(msg.body,long_test_message, 'SMS sent body is eqal to webapp body sent.');
//				equal(msg.address,message_address, 'SMS sent address is eqal to webapp address sent.');
//		});
//		
//	    setTimeout(function(){start();}, 1000);
//	    
//	});
//},8000);




module('wstTemplate');
/* The handlebar templates are returned with an \n at the end */
test('test template', function() {
	var data = {title: 'Testtitel', firstName:'Stefan',lastName:'Lexow'};
	var html = wstTemplate.get('test', data);
	var cmp =  "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
	equal(html, cmp, 'Template "test" correctly fetched.');
});

//test('contact entry template', function() {
//	var html = wstTemplate.get('contact_entry', contact_test_entry);
//	equal(html, contact_tempalte_result, 'Template "contact_entry" correctly fetched.');
//});

//test('sms thread entry template', function() {
//	var data = {title: 'Testtitel', firstName:'Stefan',lastName:'Lexow'};
//	var html = wstTemplate.get('sms_thread_entry', data);
//	var cmp =  "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
//	equal(html, cmp, 'Template "test" correctly fetched.');
//});