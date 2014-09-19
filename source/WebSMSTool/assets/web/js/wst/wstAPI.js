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
 
/**
 * api object with the following requests
 * init()
 * --> get_contacts full
 * 
 * poll
 * -->get unread sms
 * -->get contact changes
 * -->get phone status
 * -->get sms send notification
 * 
 * 
 * fetch smsthreads(user_id)
 * --> get sent and received sms for this user id --> append it to the usertab
 * 
 * 
 * USAGE policy
 * Every api request provides a callback function as very last parameter which will get
 * the data object from the success $.ajax request.
 */
(function(window) {
    wstLog.log('Initialize wstAPI.');
    var wstAPI = {
        options : {
            api_url : 'api.html'
        },

        sendRequest : function(method, parameters, callback) {
        	var methodData = "";
        	if(parameters.length == 0){
        		methodData = '{"method":"' + method + '"}';
        	} else {
        		methodData = '{"method":"' + method + '","params": ' + parameters +'}';
        	}
            $.ajax({
                url: this.options.api_url,
                success: function(data){
                  if(callback != null && data.state == 'success'){
                	  callback(data);
                  } else if(data.state == 'error') {
                	  wstLog.log(data);
                  } else {
                	  wstLog.log('ERROR wstAPI.');
                  }
                },
                type: 'post',
                data : methodData,
                dataType : "json"
              });
        },
        
        //API calls
        pollInfo: function(callback){
        	this.sendRequest("info","",callback);
        },
        
        
        sendSMSMessage: function(address, message, callback){
        	wstLog.log('sendSMSMessage called.');
        	var params = '[{"address":"'+address+'","message":"'+message+'"}]';
        	this.sendRequest("send_sms_message",params,callback);
        },
        
        
        fetchSMSThread: function(contact_id, callback){
        	wstLog.log('fetchSMSThread called.');
        	var params = '[{"contact_id":"'+contact_id+'"}]';
        	this.sendRequest("fetch_sms_thread",params,callback);        	
        },
        
        
        getContacts: function(callback){
            wstLog.log('getContacts called.');
            this.sendRequest("get_contacts","",callback);
        }
    };

    window.wstAPI = wstAPI;
})(window);
