import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  NativeModules,
  requireNativeComponent,
  View,
  Image,
  UIManager,
  DeviceEventEmitter,
  findNodeHandle
} from 'react-native';
import PropTypes from 'prop-types';

export var { EzvizModule } = NativeModules;

export var EZPTZCommand = {
  EZPTZCommandLeft: EzvizModule.EZPTZCommand_EZPTZCommandLeft,
  EZPTZCommandRight: EzvizModule.EZPTZCommand_EZPTZCommandRight,
  EZPTZCommandUp: EzvizModule.EZPTZCommand_EZPTZCommandUp,
  EZPTZCommandDown: EzvizModule.EZPTZCommand_EZPTZCommandDown,
};

export var EZPTZAction = {
  EZPTZActionStart: EzvizModule.EZPTZAction_EZPTZActionSTART,
  EZPTZActionStop: EzvizModule.EZPTZAction_EZPTZActionSTOP
};

export default class EzvizView extends Component {
  constructor(props) {
    super(props);
    const { deviceSerial, verifyCode } = this.props;

    this.deviceSerial = deviceSerial;
    this.verifyCode = verifyCode;

    this.state = {
      showPoster: !!props.poster
    }
  }

  componentDidMount() {
    this._listeners = [
      DeviceEventEmitter.addListener('EzvizPlayEvent', this._onEzvizEvent)
    ];
  }

  componentWillUnmount() {
    this._listeners && this._listeners.forEach(listener => listener.remove());
  }


  componentWillReceiveProps() {
    const { deviceSerial, verifyCode } = this.props;
    if (this.deviceSerial !== deviceSerial || this.verifyCode !== verifyCode) {
      this.deviceSerial = deviceSerial;
      this.verifyCode = verifyCode;

      this.getDeviceInfo();
      this.forceUpdate();
    }
  }

  _assignRoot = (component) => {
    this._root = component;
  }

  _getViewManagerConfig = viewManagerName => {
    if (!NativeModules.UIManager.getViewManagerConfig) {
      return NativeModules.UIManager[viewManagerName];
    }
    return NativeModules.UIManager.getViewManagerConfig(viewManagerName);
  };

  async getDeviceInfo() {
    if (!this.deviceSerial)
      return null;

    try {
      this.deviceInfo = await EzvizModule.getDeviceInfo(this.deviceSerial);
    } catch (error) {
      this.deviceInfo = null;
    }

    return this.deviceInfo;
  }

  play() {
    let cmd = {
      type: 'play'
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.play,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })
  }

  stop() {
    let cmd = {
      type: 'stop'
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.stop,
      [cmd],
    );
    // this.setNativeProps({
    //   command: cmd
    // })
  }

  openSound() {
    let cmd = {
      type: 'openSound'
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.openSound,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })
  }

  closeSound() {
    let cmd = {
      type: 'closeSound'
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.closeSound,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })
  }

  async controlPTZ(command, action) {
    if (!this.deviceInfo) {
      await this.getDeviceInfo();
    }

    if (!this.deviceInfo || !this.deviceInfo.isOnline || !this.deviceInfo.isSupportPTZ)
      return false;

    let cmd = {
      type: 'controlPTZ',
      command: command,
      action: action
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.controlPIZ,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })

    return true;
  }

  async startVoiceTalk() {
    if (!this.deviceInfo) {
      await this.getDeviceInfo();
    }
    if (!this.deviceInfo || !this.deviceInfo.isOnline || !this.deviceInfo.isSupportTalk)
      return false;

    let cmd = {
      type: 'startVoiceTalk',
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.startVoiceTalk,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })

    return true;
  }

  async stopVoiceTalk() {
    if (!this.deviceInfo) {
      await this.getDeviceInfo();
    }
    if (!this.deviceInfo || !this.deviceInfo.isOnline || !this.deviceInfo.isSupportTalk)
      return false;

    let cmd = {
      type: 'stopVoiceTalk',
    }

    UIManager.dispatchViewManagerCommand(
      this._ezvizHandle,
      this._getViewManagerConfig('Ezviz').Commands.stopVoiceTalk,
      [cmd],
    );

    // this.setNativeProps({
    //   command: cmd
    // })

    return true;
  }

  setNativeProps(nativeProps) {
    if (this._ezvizRef)
      this._ezvizRef.setNativeProps(nativeProps);
  }

  _setReference = ref => {
    if (ref) {
      this._ezvizRef = ref;
      this._ezvizHandle = findNodeHandle(ref);
    } else {
      this._ezvizRef = null;
      this._ezvizHandle = null;
    }
  };

  _onEzvizEvent = (evt) => {
    const { onEzvizEvent } = this.props;
    if (evt.type == 'MSG_REALPLAY_PLAY_SUCCESS') {
      if (this.props.poster)
        this.setState({
          showPoster: false
        })
    } else if (evt.type == 'MSG_REALPLAY_STOP_SUCCESS' || evt.type == 'MSG_REALPLAY_PLAY_FAIL') {
      if (this.props.poster)
        this.setState({
          showPoster: true
        })
    }

    if (onEzvizEvent) {
      onEzvizEvent(evt);
    }
  }

  render() {
    const { deviceSerial, verifyCode } = this.props;
    const posterStyle = {
      ...StyleSheet.absoluteFillObject,
      ...this.props.style,
      resizeMode: this.props.posterResizeMode || 'contain',
    };

    return (
      <View style={[this.props.style]}>
        <Ezviz ref={this._setReference} style={StyleSheet.absoluteFill} deviceSerial={deviceSerial} verifyCode={verifyCode} />
        {this.state.showPoster && (
          <Image style={posterStyle} source={this.props.poster} />
        )}
      </View>
    )
  }
}

EzvizView.propTypes = {
  deviceSerial: PropTypes.string.isRequired,
  verifyCode: PropTypes.string.isRequired,
  onEzvizEvent: PropTypes.func
}

const Ezviz = requireNativeComponent('Ezviz');