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
 
(function(){
    
    show_loading_animation();
    
    
    var polling_interval = 10000;
        
    /** GENERAL INITIALIZATION */    
    //init tabbing
    var tab_div = $('#tabs');
    tab_div.wstTab();
    //$('#test_button').on('click',function(){
    //    tab_div.wstTab('add',12,'name','html');
    //});
    
    //initialize the contact list
    this.number_to_contact_id = new Object(); //new Object used as map    
    wstAPI.getContacts(generate_contact_list);

    setInterval(function(){
        wstAPI.pollInfo(update_webapp);
    }, polling_interval);
    
    
    /** LISTENERS */
    $('#contact_list').on('click','div',function(){
        var contact = $(this).data('contactFull');
        organize_contact_tab(contact);
    });

    
    tab_div.on('click','.send_sms_submit',function(){
        var contact_id = $(this).data('contactId');
        send_form(contact_id);
    });
    
    
    tab_div.on('click','.contact_tab_close',function(){
        var id = $(this).data('contactId');
        if(tab_div.wstTab('remove',id)){
            wstLog.info('Contact-Tab successfully closed. To re-open it click on the entry in the contact list again.')
        }
    });
    
    
    
    /** WEBAPP METHODS */
    
    
    function show_loading_animation(){
        $.blockUI({ 
            message: $('#loading'), 
            css: { 
                border: '3px solid #225DA5',  
                'border-radius': '10px' 
            }
        });
    }
    
    
    function hide_loading_animation(timeout){
        if(timeout !== undefined && timeout > 0){
            setTimeout($.unblockUI, timeout);
        } else {
            $.unblockUI;
        }
    }
    
    
    function update_webapp(json){
        
        if(json.contact_changed === true){
            wstLog.log('Contacts changed - updating contact list.');
            generate_contact_list(json);
        }
        
        if(json.signal.signal_icon){
            var img = 'data:image/png;base64,'+json.signal.signal_icon;
            $('#signal_img').attr('src',img);
        }

        if(json.battery.battery_level_icon){
            var img = 'data:image/png;base64,'+json.battery.battery_level_icon;
            $('#batter_img').attr('src',img);
        }
        
        if(json.sms_sent_success === true){
            if(json.sms_sent_success_messages){
                sent_length = json.sms_sent_success_messages.length;
                for(var i = 0; i < sent_length; i++){
                    var address = json.sms_sent_success_messages[i].address;
                    var contact_id = get_contact_id_by_number(address);
                    if(contact_id > 0){
                        var contact = get_contact_full_name(contact_id);
                        wstLog.success('SMS to '+contact+' successfully sent!');
                        load_thread_list(contact_id);
                    } else {
                        wstLog.success('SMS to '+json.sms_sent_success_messages[i].address+' successfully sent!');
                    }
                }
            }
        }
        
        if(json.sms_received === true){
            if(json.sms_received_messages){
                recv_length = json.sms_received_messages.length;
                for(var i = 0; i < recv_length; i++){
                    var address = json.sms_received_messages[i].address;
                    var contact_id = get_contact_id_by_number(address);
                    if(contact_id > 0){
                        var contact = get_contact_full_name(contact_id);
                        wstLog.success('SMS from '+contact+' received!');
                        load_thread_list(contact_id);
                    } else {
                        wstLog.success('SMS for '+json.sms_received_messages[i].address+' received.');
                    }
                }
            }            
        }
        wstLog.log('Updating webapp');
    }
    
    
    
    
    function organize_contact_tab(contact_json){
        var contact_tab = $('#contact_tab_'+contact_json.id);
        if(contact_tab.length) { //legal js way to evaluate element exists
            tab_div.wstTab('show_tab',contact_json.id)
        } else {
            var contact_name = contact_json.display_name;
            var html = wstTemplate.get('contact_tab',contact_json);
            tab_div.wstTab('add', contact_json.id, contact_name, html);
            //wstAPI.fetchSMSThread(contact_json.id, generate_sms_thread_list);
            load_thread_list(contact_json.id);
        }
    }
    
    
    function load_thread_list(contact_id){
        var contact_tab = $('#contact_tab_'+contact_id);
        if(contact_tab.length) {
            wstAPI.fetchSMSThread(contact_id, generate_sms_thread_list);
        }
    }
    
    
    
    function generate_sms_thread_list(json)
    {
        if(json != null){
            var contact_id = json.contact_id;
            var thread_length = json.thread_messages.length;
            if(thread_length > 0){
                var html = '';
                for(var i = 0; i < thread_length; i++){
                    html += wstTemplate.get('sms_thread_entry', json.thread_messages[i])+'\n';
                }
                $('#sms_thread_'+contact_id).html(html);
                wstLog.info('Thread-List successfully loaded.');
                return true;
            } else {
                var contact_name = get_contact_full_name(contact_id);
                if(contact_name){
                    wstLog.info('No messages stored for contact '+contact_name);
                } else {
                    wstLog.info('No messages stored for this contact.');
                }
                $('#sms_thread_'+contact_id).html("");
                return true;
            }
        }
        wstLog.warn('SMS-Thread-List could not be loaded.');
        return false;
    }
    
    
    function generate_contact_list(json){
        if(json != null){
            var cl_length = json.contacts.length;
            if(cl_length > 0){
                this.number_to_contact_id = new Object();
                var html = '';
                for(var i = 0; i < cl_length; i++){
                    html += wstTemplate.get('contact_entry', json.contacts[i])+'\n';
                    
                    //store phone numbers to contacts internal
                    var phone_numbers = json.contacts[i].phone_numbers; 
                    for(var j = 0; j < phone_numbers.length; j++){
                        var number = phone_numbers[j].clean_number;
                            this.number_to_contact_id[number] = json.contacts[i].id;
                    }
                }
                $('#contact_list').html(html);
                hide_loading_animation(1000);                
                wstLog.success('Contact-List successfully updated.');
                wstLog.log(this.number_to_contact_id);
                return true;
            }
        }
        hide_loading_animation();
        wstLog.warn('Contact-List could not be loaded.');
        return false;
    }
    
    
    
    
    function get_contact_from_client(contact_id){
        var contact_entry = $('#contact_entry_'+contact_id);
        if(contact_entry.length){
            var data = contact_entry.data('contactFull');
            return data;
        } else {
            return null;
        }
    }
    
    
    
    function get_contact_id_by_number(number){
        var id = number_to_contact_id[number];
        if(id){
            return id;
        } else {
            return 0;
        }
    }
    
    
    function get_contact_full_name(contact_id){
        var contact = get_contact_from_client(contact_id);
        if(contact){
            return contact.display_name;
        } else {
            return null;
        }
    }

    
    function send_form(contact_id){
        var form = document.getElementById('send_form_'+contact_id);
        if(form.length){
            var address = form.number.options[form.number.selectedIndex].value;
            var message = encodeURI(form.sms_text.value);
            
            if(address.length > 0 && message.length > 0){
                wstAPI.sendSMSMessage(address, message, function(json){
                    //we will reach the callback only if the api request returns success-state
                    wstLog.info('Message successfully transfered to application for sending.');
                    form.sms_text.value = "";
                });
            } else {
                wstLog.log('Number and/or SMS-Text could not be correctly determined.');
            }
        } else {
            wstLog.log('ERROR: Could not determine send form with given contact_id: '+contact_id);
        }
        
    }
    
    
})();
