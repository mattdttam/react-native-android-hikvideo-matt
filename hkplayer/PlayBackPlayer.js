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
  ToastAndroid,
} from 'react-native';
import {PLAYER_STATUS, PLAYER_COMMANDS} from './HkplayerConstant';
import HkplayerPlayBackView from './HkplayerPlayBackView';

export default class PlayBackPlayer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      status: PLAYER_STATUS.IDLE,
      mPausing: false,
      mSoundOpen: false,
      mRecording: false,
      mTalking: false,
    };
  }

  componentDidMount() {
    var _self = this;
    this.listner = DeviceEventEmitter.addListener(
      'HKPLAYER_PLAY_BACK_STATUS',
      ret => {
        if (this.props.uri == ret.uri) {
          this.setState({
            status: ret.status,
            mPausing: ret.mPausing,
            mSoundOpen: ret.mSoundOpen,
            mRecording: ret.mRecording,
            mTalking: ret.mTalking,
          });
          console.log('HKPLAYER_PLAY_BACK_STATUS: ' + JSON.stringify(ret));
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
      if(this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableVoice) {
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

  renderRecordImage() {
    {
    if(this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableRecord) {
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
        <HkplayerPlayBackView
          ref={component => (this.player = component)}
          style={styles.player}
          uri={this.props.uri}
          segments={this.props.segments}
        />
        <View style={styles.container}>
          <View style={styles.line}>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => {
                  this.executeCommand(PLAYER_COMMANDS.START);
                }}
                disabled={this.state.status == PLAYER_STATUS.SUCCESS}> 
                <Image
                  style={styles.itemImage}
                  source={
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? require('./images/play-dis.png')
                      : require('./images/play.png')
                  }
                />
                <Text
                  style={
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? styles.itemTextDis
                      : styles.itemText
                  }>
                  回放
                </Text>
              </TouchableOpacity>
            </View>
            <View style={styles.item}>
              <TouchableOpacity
                style={styles.aButton}
                onPress={() => this.executeCommand(PLAYER_COMMANDS.PAUSE)}
                disabled={this.state.status != PLAYER_STATUS.SUCCESS}>
                <Image
                  style={styles.itemImage}
                  source={
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? require('./images/pause.png')
                      : require('./images/pause-dis.png')
                  }
                />
                <Text
                  style={
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? styles.itemText
                      : styles.itemTextDis
                  }>
                  {this.state.mPausing?'恢复':'暂停'}
                </Text>
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
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? require('./images/stop.png')
                      : require('./images/stop-dis.png')
                  }
                />
                <Text
                  style={
                    this.state.status == PLAYER_STATUS.SUCCESS
                      ? styles.itemText
                      : styles.itemTextDis
                  }>
                  停止
                </Text>
              </TouchableOpacity>
            </View>
			<View style={styles.item}>
            </View>
          </View>
		  {
			!this.props.hideExButtons?(
			  <View style={styles.line}>
				<View style={styles.item}>
				  <TouchableOpacity
					style={styles.aButton}
					onPress={() => this.executeCommand(PLAYER_COMMANDS.SOUND)}
					disabled={this.state.status != PLAYER_STATUS.SUCCESS || this.props.disableVoice}>
					<Image
					  style={styles.itemImage}
					  source={this.renderVoiceImage()}
					/>
					<Text
					  style={
						this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableVoice
						  ? styles.itemText
						  : styles.itemTextDis
					  }>
					  {this.state.mSoundOpen == true ? '静音' : '开启声音'}
					</Text>
				  </TouchableOpacity>
				</View>
				<View style={styles.item}>
				  <TouchableOpacity
					style={styles.aButton}
					onPress={() => this.executeCommand(PLAYER_COMMANDS.CAPTURE)}
					disabled={this.state.status != PLAYER_STATUS.SUCCESS || this.props.disableCapture}>
					<Image
					  style={styles.itemImage}
					  source={
						this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableCapture
						  ? require('./images/camera.png')
						  : require('./images/camera-dis.png')
					  }
					/>
					<Text
					  style={
						this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableCapture
						  ? styles.itemText
						  : styles.itemTextDis
					  }>
					  截屏
					</Text>
				  </TouchableOpacity>
				</View>
				<View style={styles.item}>
				  <TouchableOpacity
					style={styles.aButton}
					onPress={() => this.executeCommand(PLAYER_COMMANDS.RECORD)}
					disabled={this.state.status != PLAYER_STATUS.SUCCESS || this.props.disableRecord}>
					<Image
					  style={styles.itemImage}
					  source={this.renderRecordImage()}
					/>
					<Text
					  style={
						this.state.status == PLAYER_STATUS.SUCCESS && !this.props.disableRecord
						  ? styles.itemText
						  : styles.itemTextDis
					  }>
					  {this.state.mRecording==true?"停止录像":"开始录像"}
					</Text>
				  </TouchableOpacity>
				</View>
				<View style={styles.item}></View>
			  </View>
			):null
		  }
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  body: {
    height: 470,
  },
  player: {
    height: 300,
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
    fontSize: 16,
    color: '#808080',
  },
  itemTextDis: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#d3d3d3',
  },
});
