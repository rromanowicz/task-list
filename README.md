#task-list

##Endpoints:
> ###GET `/`
> **Params:**
>>* None
> 
> **Return:**
>>* HttpStatus
>>* String

> ###POST `/api/user/create`
> **Params:**
>>* token: String
>>* user: User
> 
> **Return:**
>>* HttpStatus
>>* User

> ###GET `/api/user/id/{id}`
> **Params:**
>>* token: String
>>* id: Long
> 
> **Return:**
>>* HttpStatus
>>* User

> ###GET `/api/user/name/{name}`
> **Params:**
>>* token: String
>>* username: String
> 
> **Return:**
>>* HttpStatus
>>* User

> ###GET `/api/user/id/{id}/delete`
> **Params:**
>>* token: String
>>* id: Long
> 
> **Return:**
>>* HttpStatus

> ###GET `/api/user/name/{name}/delete`
> **Params:**
>>* token: String
>>* username: String
> 
> **Return:**
>>* HttpStatus

> ###POST `/api/taskList/create`
> **Params:**
>>* token: String
>>* TaskList
> 
> **Return:**
>>* HttpStatus
>>* taskList: TaskList

> ###GET `/api/taskList/get/id/{id}`
> **Params:**
>>* token: String
>>* id: Long
> 
> **Return:**
>>* HttpStatus
>>* TaskList

> ###GET `/api/taskList/get/user/{username}`
> **Params:**
>>* token: String
>>* username: String
> 
> **Return:**
>>* HttpStatus
>>* List<TaskList>

> ###GET `/api/taskList/{id}/delete`
> **Params:**
>>* token: String
>>* id: long
> 
> **Return:**
>>* HttpStatus

> ###GET `/api/taskList/{id}/share/{username}`
> **Params:**
>>* token: String
>>* id: Long
>>* username: String
> 
> **Return:**
>>* HttpStatus
>>* String

> ###GET `/api/taskList/{id}/unShare/{username}`
> **Params:**
>>* token: String
>>* id: Long
>>* username: String
>
> **Return:**
>>* HttpStatus
>>* String

> ###POST `/api/taskList/{id}/task/add`
> **Params:**
>>* token: String
>>* id: Long
>>* task: Task
> 
> **Return:**
>>* HttpStatus
>>* Task

> ###GET `/api/taskList/{id}/task/getAll`
> **Params:**
>>* token: String
>>* id: Long
> 
> **Return:**
>>* HttpStatus
>>* List<Task>

> ###GET `/api/taskList/{listId}/task/{taskId}/delete`
> **Params:**
>>* token: String
>>* listId: Long
>>* taskId: Long
> 
> **Return:**
>>* HttpStatus

> ###GET `/api/taskList/{listId}/task/{taskId}/completed/{completed}`
> **Params:**
>>* token: String
>>* listId: Long
>>* taskId: Long
>>* completed: boolean
> 
> **Return:**
>>* HttpStatus

> ###POST `/api/taskList/{listId}/update`
> **Params:**
>>* token: String
>>* listId: Long
>>* taskList: TaskList
> 
> **Return:**
>>* HttpStatus

> ###POST `/api/taskList/{listId}/task/{taskId}/update`
> **Params:**
>>* token: String
>>* listId: Long
>>* taskId: Long
>>* task: Task
> 
> **Return:**
>>* HttpStatus