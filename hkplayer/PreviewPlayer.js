import React, {Component} from 'react';
import {
  requireNativeComponent,
  View,
  UIManager,
  findNodeHandle,
  AppState,
  DeviceEventEmitter,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
} from 'react-native';
import {PLAYER_STATUS, PLAYER_COMMANDS} from './HkplayerConstant';
import HkplayerView from './HkplayerView';

export default class PreviewPlayer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      appState: AppState.currentState,
      status: PLAYER_STATUS.IDLE,
      //mPausing: false,
      mSoundOpen: true,
      mRecording: false,
      mTalking: false,
    };
  }

  componentDidMount() {
    var _self = this;
    this.listener = DeviceEventEmitter.addListener(
      'HKPLAYER_PREVIEW_STATUS',
      ret => {
        if (_self.state.uri == ret.uri) {
          this.setState({
            status: ret.status,
            //mPausing: ret.mPausing,
            mSoundOpen: ret.mSoundOpen,
            mRecording: ret.mRecording,
            mTalking: ret.mTalking,
          });
          console.log('HKPLAYER_PREVIEW_STATUS: ' + JSON.stringify(ret));
        }
      },
    );
  }

  componentWillUnmount() {
    if (this.listener) {
      this.listener.remove();
    }
  }

  executeCommand(command) {
    this.player.executeCommand(command);
  }

  renderVoiceImage() {
    {
      if(this.state.status == PLAYER_STATUS.SUCCESS) {
        if(this.state.mSoundOpen==false) {
          return require('./images/mute.png');
        } else {
          return require('./images/volume.png');
        }
      } else {
        if(this.state.mSoundOpen==false) {
          return require('./images/mute-dis.png');
        } else {
          return require('./images/volume-dis.png');
        }
      }
    }
  }

  renderMicImage() {
    {
    if(this.state.status == PLAYER_STATUS.SUCCESS) {
        if(this.state.mTalking==false) {
          return require('./images/mic-muted.png');
        } else {
          return require('./images/microphone.png');
        }
      } else {
        if(this.state.mTalking==false) {
          return require('./images/mic-muted-dis.png');
        } else {
          return require('./images/microphone-dis.png');
        }
      }
    }
  }

  renderRecordImage() {
    {
    if(this.state.status == PLAYER_STATUS.SUCCESS) {
        if(this.state.mRecording==false) {
          return require('./images/no-camcorder.png');
        } else {
          return require('./images/camcorder.png');
        }
      } else {
        if(this.state.mRecording==false) {
          return require('./images/no-camcorder-dis.png');
        } else {
          return require('./images/camcorder-dis.png');
        }
      }
    }
  }

  render() {
    return (
      <View style={styles.body}>
        <HkplayerView
          ref={component => (this.player = component)}
          style={styles.player}
          uri={this.props.uri}
        />
        <View style={styles.container}>
          <View style={styles.line}>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => {this.executeCommand(PLAYER_COMMANDS.START)}}
                disabled={this.state.status == PLAYER_STATUS.SUCCESS}
              >
                <Image
                  style={styles.itemImage}
                  source={
                    this.state.status==PLAYER_STATUS.SUCCESS?require('./images/play-dis.png'):require('./images/play.png')
                  }
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemTextDis:styles.itemText}>播放</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.STOP)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={
                    this.state.status==PLAYER_STATUS.SUCCESS?require('./images/stop.png'):require('./images/stop-dis.png')
                  }
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemText:styles.itemTextDis}>停止</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.SOUND)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={this.renderVoiceImage()}
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemText:styles.itemTextDis}>
                  {this.state.mSoundOpen==true?"静音":"开启声音"}
                </Text>
              </TouchableOpacity>
            </View>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.TALK)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={this.renderMicImage()}
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemText:styles.itemTextDis}>
                  {this.state.mTalking==false?"通话":"关闭通话"}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
          <View style={styles.line}>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.CAPTURE)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={ this.state.status==PLAYER_STATUS.SUCCESS?require('./images/camera.png'):require('./images/camera-dis.png')}
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemText:styles.itemTextDis}>截屏</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.item} hide={this.state.mRecording}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.RECORD)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={this.renderRecordImage()}
                />
                <Text style={this.state.status==PLAYER_STATUS.SUCCESS?styles.itemText:styles.itemTextDis}>开启录像</Text>
              </TouchableOpacity>
            </View>
             <View style={styles.item}>
            </View>
             <View style={styles.item}>
            </View>
          </View>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  body: {
   height: 420,
  },
  player: {
    height: 250,
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'flex-start',
    alignItems: 'stretch',
  },
  line: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  item: {
    flex: 1,
  },
  aButton: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
  },
  itemImage: {
    height: 40,
    resizeMode: 'contain',
    marginTop: 10,
    marginBottom: 10,
  },
  itemText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#808080',
  },
  itemTextDis: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#d3d3d3',
  },
});