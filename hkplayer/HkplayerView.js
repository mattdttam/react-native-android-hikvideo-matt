import React, {Component} from 'react';
import {
  requireNativeComponent,
  View,
  UIManager,
  findNodeHandle,
  AppState,
} from 'react-native';
import {PLAYER_COMMANDS} from './HkplayerConstant';

const RCT_PLAYER_REF = 'HkplayerView';
export default class HkplayerView extends React.Component {
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
      UIManager.HkplayerView.Commands.executeCommand,
      [command],
    );
  }

  render() {
    return <RCTView ref={RCT_PLAYER_REF} {...this.props} />;
  }
}

const RCTView = requireNativeComponent('HkplayerView', HkplayerView, {
  nativeOnly: {onChange: true},
});
