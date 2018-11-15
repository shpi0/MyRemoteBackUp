package message;

/**
 * Список доступных типов сообщений
 */
public enum MessageType {
    FILE_REQUEST,  // Request a file from server
    FILE_DELETE,   // Delete file on the server
    FILE_DELETE_OK,
    LOGIN_ATTEMPT, // Try login to server
    LOGIN_FAILED,  // Failed login message
    LOGIN_SUCCESS,  // Ok login message
    REGISTER_REQUEST,
    REGISTER_DONE,
    REGISTER_FAIL,
    FILE_LIST,
    FILE_LIST_REQUEST,
    FILE_RECEIVED_SUCCESS,
    FILE_RENAME,
    FILE_RENAME_SUCCESS,
    FILE_RENAME_FAIL
}
