cordova.define("at.modalog.cordova.plugin.html2pdf.Html2pdf", function (require, exports, module) {
    //////////////////////////////////////////
    // Html2pdf.js
    // Copyright (C) 2014 Modern Alchemits OG <office@modalog.at>
    //
    //////////////////////////////////////////
    var exec = require('cordova/exec');

    var Html2pdf = {
        create: function (html, filePath, options, success, error) {
            exec(success, error, "Html2pdf", "create", [html, filePath, options]);
        },

        mergePDFs: function (filePath, files, success, error) {
            console.log('exex some cordova plugin please');
            exec(success, error, "Html2pdf", "mergePDFs", [filePath, files]);
        }
    };

    module.exports = Html2pdf;

});
