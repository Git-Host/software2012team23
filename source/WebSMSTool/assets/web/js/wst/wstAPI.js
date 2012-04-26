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
            api_url : 'api.html',
        },

        //API calls
        pollInfo: function(callback){
            wstLog.log('PollInfo called.');
            $.ajax({
              url: this.options.api_url,
              success: function(data){
            	  if(data.state == 'success'){
            		  callback(true);
            	  } else {
            		  callback(false);
            	  }
              },
              error: function(data){
            	  callback(false);
              },
              type: 'post',
              data : "{\"method\": \"info\",\"params\": [{\"id\": \"1\"}]}"
            });
        },
        
        
        getContacts: function(callback){
            wstLog.log('getContacts called.');
            $.ajax({
              url: this.options.api_url,
              success: function(data){
            	  callback(data);
            	  //only for testing
            	  if(data.image != null){
            		  $('#testimg').attr('src', data.image);
            	  }
              },
              type: 'post',
              data : "{\"method\": \"get_contacts\"}"
            });
        }
   
    };

    window.wstAPI = wstAPI;
})(window);