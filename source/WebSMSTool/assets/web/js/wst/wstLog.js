(function(window) {
    var wstLog = {
    	debug : true,
        log: function(message){
            if(console && this.debug){
                console.log(message);            
            }
        }
    };
    window.wstLog = wstLog;
})(window);