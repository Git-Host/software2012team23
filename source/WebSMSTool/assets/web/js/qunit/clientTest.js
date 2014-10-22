/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var short_test_message = "This is an short message.";
var long_test_message = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
var message_address = "1234567890";

// we turn async to off, so our tests will run in order and we can predict the
// results.
$.ajaxSetup({
	cache : false,
	async : false
});

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

setTimeout(function() {
	module('wstAPI');
	test('API - Simple Tests', function() {
		stop();
		expect(4);

		wstAPI.pollInfo(function(data) {
			ok(data.state == 'success', 'PollInfo request is working.');
		});

		wstAPI.getContacts(function(data) {
			ok(data.state == 'success', 'Get Contacts successfully received');
		});

		wstAPI.sendSMSMessage(message_address, short_test_message, function(
				data) {
			ok(data.state == 'success',
					'SMS successfully sent to phone application.');
		});

		wstAPI.fetchSMSThread(1, function(data) {
			ok(data.state == 'success', 'SMS Thread successfully fetched.');
		});

		setTimeout(function() {
			start();
		}, 1000);
	});

	test('API - Fetch sms thread delivers non "null" person', function() {
		stop();
		expect(2);

		wstAPI.fetchSMSThread(1, function(data) {
			ok(data.state == 'success', 'SMS Thread successfully fetched.');

			var success = true;
			for (var i = 0; i < data.thread_messages.length; i++) {
				if (data.thread_messages[i].person == "null") {
					success = false;
					break;
				}
			}
			ok(success === true,
					'No SMS Messages without person attribute == null found.');
		});

		setTimeout(function() {
			start();
		}, 1000);
	});

}, 3000);

setTimeout(
		function() {
			module('wstAPI');
			test(
					'API - Test sms sent notficiation',
					function() {
						stop();
						expect(4);

						wstAPI
								.pollInfo(function(data) {
									ok(data.sms_sent_success == true,
											'SMS sent success returned true.');
									ok(
											data.sms_sent_success_messages.length > 0,
											'SMS success messages are set.');
									var msg = data.sms_sent_success_messages[0];
									equal(msg.body, short_test_message,
											'SMS sent body is eqal to webapp body sent.');
									equal(msg.address, message_address,
											'SMS sent address is eqal to webapp address sent.');
								});
						setTimeout(function() {
							start();
						}, 1000);
					});
		}, 4000);

setTimeout(
		function() {
			module('wstAPI');
			test(
					'API - Test long text sms',
					function() {
						stop();
						expect(1);

						wstAPI
								.sendSMSMessage(
										message_address,
										long_test_message,
										function(data) {
											ok(data.state == 'success',
													'Long SMS successfully sent to phone application.');
										});

						setTimeout(function() {
							start();
						}, 1000);

					});
		}, 6000);

setTimeout(
		function() {
			module('wstAPI');
			test(
					'API - Test long text sms sent notification',
					function() {
						stop();
						expect(4);

						wstAPI
								.pollInfo(function(data) {
									ok(data.sms_sent_success == true,
											'SMS sent success returned true.');
									ok(
											data.sms_sent_success_messages.length > 0,
											'SMS success messages are set.');
									var msg = data.sms_sent_success_messages[0];
									equal(msg.body, long_test_message,
											'SMS sent body is eqal to webapp body sent.');
									equal(msg.address, message_address,
											'SMS sent address is eqal to webapp address sent.');
								});

						setTimeout(function() {
							start();
						}, 1000);

					});
		}, 8000);

module('wstTemplate');
/* The handlebar templates are returned with an \n at the end */
test('test template', function() {
	var data = {
		title : 'Testtitel',
		firstName : 'Stefan',
		lastName : 'Lexow'
	};
	var html = wstTemplate.get('test', data);
	var cmp = "<h1>Testtitel</h1><h2>By Stefan Lexow</h2>\n";
	equal(html, cmp, 'Template "test" correctly fetched.');
});