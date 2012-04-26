(function(window) {
	
    var wstLog = {
        log: function(message){
            if(console){
                console.log(message);            
            }
        }
    };

    window.wstLog = wstLog;


    wstLog.log('Initialize wstJS object.');

    var wstJS = {
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
              data : "{\"method\": \"get_contacts\"}"
            });
        }
        
        
        
        
        
        
        
        
        
        
    };


    window.wstJS = wstJS;
})(window);

//setInterval(wstJS.pollInfo(), 1000);