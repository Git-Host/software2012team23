//disable async for loading the needed client scripts
$.ajaxSetup({cache: true, async: false });
jQuery.getScript('js/wst/wstLog.js');
jQuery.getScript('js/wst/wstAPI.js');
$.ajaxSetup({cache: false, async: true });

