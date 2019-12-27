import React, {Component} from 'react';
import {
  requireNativeComponent,
  View,
  UIManager,
  findNodeHandle,
  AppState,
} from 'react-native';
import {PLAYER_STATUS, PLAYER_COMMANDS} from './HkplayerConstant';

const RCT_PLAYER_REF = 'SimpleHkplayerPlayBackView';
export default class SimpleHkplayerPlayBackView extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      appState: AppState.currentState,
    };
  }

  componentDidMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
  }

  _handleAppStateChange = nextAppState => {
    if (
      this.state.appState.match(/inactive|background/) &&
      nextAppState === 'active'
    ) {
      this.executeCommand(PLAYER_COMMANDS.ONRESUME);
    } else {
      this.executeCommand(PLAYER_COMMANDS.ONPAUSE);
    }
    this.setState({appState: nextAppState});
  };

  executeCommand(command) {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.refs[RCT_PLAYER_REF]),
      UIManager.SimpleHkplayerPlayBackView.Commands.executeCommand,
      [command],
    );
  }

  render() {
    return <RCTView ref={RCT_PLAYER_REF} {...this.props} />;
  }
}

const RCTView = requireNativeComponent(
  'SimpleHkplayerPlayBackView',
  SimpleHkplayerPlayBackView,
  {
    nativeOnly: {onChange: true},
  },
);
