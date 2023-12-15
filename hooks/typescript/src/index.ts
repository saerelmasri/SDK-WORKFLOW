import { Celitech, Environment } from '..';
import HTTPLibrary from '../http/HTTPLibrary';

let CURRENT_TOKEN = '';
let CURRENT_EXPIRY = -1;

/**
 * Request to the API
 */
export interface Request {
  method: string;
  url: string;
  input?: object;
  headers: object;
}

/**
 * Response from the API
 */
export interface Response {
  data: object;
  headers: object;
  status: number;
}

/**
 * Exception thrown by the API
 */
export interface Exception extends Error {
  title: string;
  type?: string;
  detail?: string;
  instance?: string;
  statusCode: number;
}

/**
 * Standard Hook interface
 */
export interface Hook {
  /**
   * Called before the request is sent to the API
   * @param request
   */
  beforeRequest(request: Request): Promise<void>;

  /**
   * Called after the response is received from the API
   * @param request
   * @param response
   */
  afterResponse(request: Request, response: Response): Promise<void>;

  /**
   * Called when an error occurs
   * @param error
   */
  onError(error: Exception): Promise<void>;
}

/**
 * Custom Hook - This is a sample hook that can be used as a template for your own hook
 * This hook will sign the request with a HMAC signature
 */
export default class CustomHook implements Hook {
  async beforeRequest(request: Request): Promise<void> {
    if (request.url.endsWith('/oauth/token')) return;
    // Get the client_id and client_secret from environment variables
    const clientId: string = process.env.CLIENT_ID || '';
    const clientSecret: string = process.env.CLIENT_SECRET || '';

    if (!clientId || !clientSecret) {
      console.error('Missing CLIENT_ID and/or CLIENT_SECRET environment variables');
      return;
    } else {
      // Check if CURRENT_TOKEN is missing or CURRENT_EXPIRY is in the past
      if (!CURRENT_TOKEN || CURRENT_EXPIRY < Date.now()) {
        const sdk = new Celitech({ environment: Environment.TOKEN_SERVER });
        // Prepare the request payload for fecthing a fresh Oauth token
        const input = {
          client_id: Number(clientId) || -1,
          client_secret: clientSecret || '',
          grant_type: 'client_credentials',
          scope: '*',
        };

        // Fetch a fresh Oauth token
        // Retrieve the new access token and expiry, and set them to the global variables
        const tokenResponse = await this.doPost(request, input, '/oauth2/token');
        const { expires_in, access_token } = tokenResponse.data;
        // const { expires_in, access_token } = await sdk.tokenService.getOauthToken(input);

        if (!expires_in || !access_token) {
          console.error('There is an issue with getting the oauth token');
          return;
        }
        CURRENT_EXPIRY = Date.now() + expires_in * 1000;
        CURRENT_TOKEN = access_token;
      }

      // Set the Bearer token in the request header
      const authorization = `Bearer ${CURRENT_TOKEN}`;
      request.headers = {
        ...request.headers,
        Authorization: authorization,
      };
    }
  }

  async doPost(request: Request, input: any, urlEndpoint: string): Promise<any> {
    const indexOfDotNet = request.url.indexOf('.net');
    const baseUrl = request.url.substring(0, indexOfDotNet + 4);
    const fullUrl = `${baseUrl}${urlEndpoint}`;

    try {
      const resp = await new HTTPLibrary().post(
        fullUrl,
        input,
        {
          'Content-type': 'application/json',
        },
        true,
      );

      return resp;
    } catch (error) {
      console.error('Error in posting the request:', error);
      return null;
    }
  }

  async afterResponse(request: Request, response: Response): Promise<void> {
    // no-op
  }

  async onError(error: Exception): Promise<void> {
    console.error('an error occurred!');
  }
}
