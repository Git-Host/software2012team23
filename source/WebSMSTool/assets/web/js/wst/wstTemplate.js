(function(window) {
	wstLog.log('Load wst template engine.');
	
    var wstTemplate = {
    		init : function(){
    			wstLog.log('Init wst templating');
    			//fetch html templates to map
    			 //$.get('js/wst/templates/test.html',function(response){
    	    		// wstLog.log('Loaded test html.');
    				 //this.test = Handlebars.compile(response);
    				 //wstLog.log('Html compiled and added to map.');
    			 //});

    		},
    		get : function(templateName, data){
    			wstLog.log('Try to return template '+templateName);
    			if(this.test != null){
    				return this.test(data);
    			}
    		}
    };
    
    wstTemplate.init();
    window.wstTemplate = wstTemplate;
})(window);