
var exec = require('cordova/exec');

var PLUGIN_NAME = 'ImageEditor';

var ImageEditorPlugin = {
  edit: function(url, success, failure) {
    exec(success, failure, PLUGIN_NAME, 'edit', [url]);
  },
};

module.exports = ImageEditorPlugin;
