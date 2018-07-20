import rxjs from 'rxjs';
import { Subject } from 'rxjs/Subject';

let rSocket;

function serialize (resp, subject) {
  subject.next(resp);
}

export default class RSocketTransport {

  constructor (wsuri) {
    this.uri = wsuri;
    this.subject = new Subject();
        // Create an instance of a client
    this.client = new rsocketCore.RSocketClient({
            // send/receive objects instead of strings/buffers
      serializers: rsocketCore.JsonSerializers,
      setup: {
                // ms btw sending keepalive to server
        keepAlive: 60000,
                // ms timeout if no keepalive response
        lifetime: 180000,
                // format of `data`
        dataMimeType: 'application/json',
                // format of `metadata`
        metadataMimeType: 'application/json',
      },
      transport: new rsocketWebsocketClient.default({ url: wsuri })
    });
  }

  connect () {
    return this.client.connect();
  }

  onStatusChanged (statusCallback) {
    this.statusChangedCallback = statusCallback;
  }

  disconnect () {
    this.status = false;
    this.client.close();
  }

  isConnected () {
    return this.status;
  }

  uri () {
    return this.uri;
  }

  next (rSocket, next) {
    const input = JSON.parse(next);
    const request = {
      data: input.data,
      metadata: input.metadata
    };

    const onnext = resp => serialize(resp, this.subject);
    const onerror = error => serialize(error, this.subject);
    const oncomplete = () => console.log('Completed!');

    rSocket.requestStream(request).subscribe({
      onSubscribe: subscription => subscription.request(2147483647),
      onNext: onnext,
      onComplete: oncomplete,
      onError: onerror
    });
  }

  listen () {
    return this.subject;
  }
}

