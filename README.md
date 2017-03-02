# OTA Updates
An App for recieving Updates on-the-fly

To see how to install the Server, please see here: [OTAServer](http://www.github.com/TimSchumi/OTAServer)

## Installation
You either need to set your server's URL in the values.xml before compiling, or you can add a custom build.prop value containing the URL.

### values.xml (Before compiling the APK)
To set your OTA URL, you need to change this line in app/src/main/res/values/values.xml:

    <string name="download_url" translatable="false">http://localhost</string> 
 
You need to replace "http://localhost" with the URL where you deployed the server (without trailing slash!)

### build.prop
To set the OTA URL in your build.prop, you need to add the following line:

    ro.ota.url=http://localhost

where "http://localhost" should be replaced with the URL where you deployed the server (without trailing slash!)  
If this prop is present, the value inside values.xml will be ignored

Prebuilt APKs will always contain http://localhost, so if you use them, you need to set the URL in the build.prop

##Contribute

You can find issues and submit them in the Issue Tracker or (if you feel like you can improve this program) fork this project on GitHub (always fork the "develop" branch) and drop me a Pull Request and I will get the changes merged after a review

##License

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

## Thanks

Special thanks to [@berkantkz](http://www.github.com/berkantkz), without whose help I would only have a OTA Server and no App that could use it
