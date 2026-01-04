export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: Record<string, string> | null;
}

export interface AxiosErrorResponse {
  response?: {
    data?: unknown;
    status?: number;
  };
  message?: string;
}

export const isApiErrorResponse = (error: unknown): error is ApiErrorResponse => {
  return (
    typeof error === "object" &&
    error !== null &&
    "timestamp" in error &&
    "status" in error &&
    "error" in error &&
    "message" in error
  );
};

export const getErrorMessage = (error: unknown): string => {
  // Handle axios error with response data
  if (
    typeof error === "object" &&
    error !== null &&
    "response" in error
  ) {
    const axiosError = error as AxiosErrorResponse;
    
    // Check if response has data with our API error format
    if (axiosError.response?.data && isApiErrorResponse(axiosError.response.data)) {
      return axiosError.response.data.message || axiosError.response.data.error || "An error occurred";
    }
    
    // Fallback to response message if it's an object with message property
    if (
      typeof axiosError.response?.data === "object" &&
      axiosError.response.data !== null &&
      "message" in axiosError.response.data
    ) {
      const dataWithMessage = axiosError.response.data as Record<string, unknown>;
      if (typeof dataWithMessage.message === "string") {
        return dataWithMessage.message;
      }
    }
  }

  if (isApiErrorResponse(error)) {
    return error.message || error.error || "An error occurred";
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unknown error occurred";
};
