(function(window) {
    var wstLog = {
    	debug : true,
        log: function(message){
            if(console && this.debug){
                console.log(message);            
            }
        },
    
    	notify: function(type,message){
    		if(type == "success"){
    		    Notifier.success(message, 'SUCCESS');    			
    		} else if(type == "warn"){
    			Notifier.warning(message, 'WARNING');
    		} else if(type == "error"){
    			Notifier.error(message, 'ERROR');
    		} else if(type == "info"){
    			Notifier.info(message, 'INFO');
    		}
    	},
    	
    	success: function(message){
    		this.notify('success',message);
    	},
    	warn: function(message){
    		this.notify('warn', message);
    	},
    	error: function(message){
    		this.notify('error', message);
    	},
    	info: function(message){
    		this.notify('info', message);
    	}
    
    };
    window.wstLog = wstLog;
})(window);