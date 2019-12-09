
# react-native-android-hikvideo-matt

## Getting started

`$ npm install react-native-android-hikvideo-matt --save`

### Mostly automatic installation

`$ react-native link react-native-android-hikvideo-matt`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.matt.rn.android.hikvideo.HkplayerViewPackage;` to the imports at the top of the file
  - Add `new HkplayerViewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-hikvideo-matt'
  	project(':react-native-android-hikvideo-matt').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-hikvideo-matt/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-android-hikvideo-matt')
  	```

## Modify android build.gradle as follow

```
splits {
        abi {
            enable true
            reset()
            include "armeabi-v7a"
        }
    }
```
```
applicationVariants.all { variant ->
        variant.outputs.each { output ->
            // For each separate APK per architecture, set a unique version code as described here:
            // https://developer.android.com/studio/build/configure-apk-splits.html
            def versionCodes = ["armeabi-v7a": 1]
            def abi = output.getFilter(OutputFile.ABI)
            if (abi != null) {  // null for the universal-debug, universal-release variants
                output.versionCodeOverride =
                        versionCodes.get(abi) * 1048576 + defaultConfig.versionCode
            }

        }
    }
```

## Usage
```javascript
import PreviewPlayer,{PlayBackPlayer} from 'react-native-android-hikvideo-matt';

render() {
    return (
      <View>
        <PreviewPlayer uri={this.state.uri} />
		// or 
		// <PlayBackPlayer uri={this.state.uri} segments={this.state.segments}/>
		// for play back
      </View>
    );
  }
```
  