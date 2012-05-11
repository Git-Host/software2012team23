(function( $ ){
	var settings = {};
	var methods = {
		init : function(options){
			settings = $.extend( {
				  'id_prefix' : 'contact_tab_'
				}, options); 
			$('div',this).hide();
			$('div:first',this).show();
			$('ul li:first',this).addClass('active');
			
			$('ul',this).on('click','li a',null,function(){
				var ul = $(this).parent().parent();
			    $('li',ul).removeClass('active');
			    
			    $(this).parent().addClass('active');
			    var currentTab = $(this).attr('href');
			    $('div',ul.parent()).hide();
			    $(currentTab).show();
			    return false;	
			});
		},
		
		add: function(id,name,div_html){
			var ul = $('ul',this);
			ul.append('<li id="li_'+id+'"><a href="#'+settings.id_prefix+id+'">'+name+'</a></li>');
			
			var new_tab_div = document.createElement('div');
			new_tab_div.setAttribute('id',settings.id_prefix+id);
			new_tab_div.innerHTML = div_html;
			$(new_tab_div).hide();
			
			this.append(new_tab_div);
		}
	}
	
	
	$.fn.wstTab = function( method ) {  
		// Method calling logic
		if ( methods[method] ) {
		  return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || ! method ) {
		  return methods.init.apply( this, arguments );
		} else {
		  $.error( 'Method ' +  method + ' does not exist on jQuery.wstTab' );
		}
	};
})( jQuery );