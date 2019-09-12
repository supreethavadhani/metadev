import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpConfig } from './httpConfig';
import { Vo, ServerResponse, FilterRequest } from './types';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
/**
 * A wrapper on HttpClient to take care of our protocols
 */
export class ServerAgent {
	static SERVICE_HEADER = '_s';
	static AUTH_HEADER = '_t';
	static TAG_MESSAGES = 'messages';
	static TAG_ALL_OK = 'allOk';
	static TAG_DATA = 'data';
	static LIST_SERVICE: 'listService';
	constructor(private http: HttpClient, private config: HttpConfig) {
	}

	/**
	 * serve this service. we use a strict service oriented architecture, 
	 * where in the only thing the client can ask the server is to serve a service.
	 * There is no concept of resources or operations. Any such concepts are to be 
	 * implemented using the service paradigm. 
	 * @param serviceName  name of the service to be requested
	 * @param data input data for the request
	 * @param asQueryParams true if the data is just a set of name-string params, and the srver expects them in query string
	 * @param headers any special headers to be communnicated. Typically for additional authentication.
	 */
	serve(serviceName: string, data: Vo|FilterRequest, asQueryParams?: boolean, headers?: { [key: string]: string }): Observable<Vo> {
		headers = headers || {};
		headers[ServerAgent.SERVICE_HEADER] = serviceName;
		headers[ServerAgent.AUTH_HEADER] = this.config.auth;
		let obs: Observable<HttpResponse<ServerResponse>>;
		if (asQueryParams) {
			const params = this.toParams(data);
			obs = this.http.post<ServerResponse>(this.config.url, null, { observe: "response", headers: headers, params: params });
		} else {
			obs = this.http.post<ServerResponse>(this.config.url, data, { observe: "response", headers: headers });
		}

		// we need to unpack the payload to get status and messages in addition to actual data
		return new Observable((observer) => {
			const { next, error } = observer;
			const subscr = obs.subscribe({
				next(resp) {
					if (!resp.ok) {
						const msg = 'Server Error. http-status :' + resp.status + '=' + resp.statusText + (resp.body ? 'Response: ' + JSON.stringify(resp.body) : '');
						console.error(msg);
						error({ type: 'error', id: 'serverError', text: msg });
						return;
					}

					//no-news is good-news!!
					if (!resp.body || resp.body === {}) {
						next({});
						return;
					}

					const { messages, allOk, data } = resp.body;
					if (allOk) {
						next(data);
						return;
					}

					if (messages) {
						error(messages);
						return;
					}
					const msg = 'Server Error. server reported a failure, but did not return any error message';
					console.error(msg);
					error({ type: 'error', id: 'serverError', text: msg });
					return;
				}, error(msgs) {
					error(msgs);
				}
			});
			return { unsubscribe() { subscr.unsubscribe() } };
		});
	}

	private toParams(data: any): HttpParams {
		let params = new HttpParams();
		for (const a in data) {
			if (data.hasOwnProperty(a)) {
				params.set(a, data[a].toString());
			}
		}
		return params;
	}
	/**
	 * initiates a file-down load by the browser with supplied data as content
	 * @param data  to be downloaded
	 * @param fileName naem of the file to be downloaded as 
	 */
	public download(data: any, fileName: string) {
		const json = JSON.stringify(data);
		const blob = new Blob([json], { type: 'octet/stream' });
		const url = window.URL.createObjectURL(blob);
		const a = window.document.createElement('a');
		a.style.display = 'none';
		a.href = url;
		a.target = '_blank';
		a.download = fileName;
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
	}
}
