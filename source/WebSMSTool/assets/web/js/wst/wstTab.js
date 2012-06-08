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
 
(function( $ ){
	var settings = {};
	var methods = {
		init : function(options){
			settings = $.extend({
				'id_prefix' : 'contact_tab_',
				'tab_selector' : this.selector 
			}, options);
			
			
			$('ul', this).on('click', 'li a' ,null, function(){
				var li = $(this).parent();
				var contact_id = li.data('listContactId');
				methods.show_tab(contact_id);
				return false;
			});

			methods.show_tab(0);
		},
		
		show_tab : function(id){
			var ul = $(settings.tab_selector+' ul');
		    $('li',ul).removeClass('active');
		    
		    $('#li_'+id).addClass('active');
		    var currentTab = $('#li_'+id+' a').attr('href');
		    
		    $('div.contact_tab',$(settings.tab_selector)).hide();
		    //$(currentTab+' div').show();
		    $(currentTab).show();
		    return true;
		},
		
		add: function(id, name, div_html){
			var ul = $('ul',this);
			ul.append('<li id="li_'+id+'" data-list-contact-id="'+id+'"><a href="#'+settings.id_prefix+id+'">'+name+'</a></li>');
			
			var new_tab_div = document.createElement('div');
			new_tab_div.setAttribute('id',settings.id_prefix+id);
			new_tab_div.setAttribute('class','contact_tab');
			new_tab_div.innerHTML = div_html;
			
			$(new_tab_div).hide();
			
			this.append(new_tab_div);
			methods.show_tab(id);
			return true;
		},
		
		remove: function(id){
			var ul = $('ul',this);
			$('#li_'+id).remove();
			$('#'+settings.id_prefix+id).remove();			
			methods.show_tab(0);
			return true;
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