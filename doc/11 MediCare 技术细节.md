## MediCare App 技术细节  
### 技术细节  
- 服务端与客户端分别维护数据库，并使用 UUID 实现双端的数据对应  
    - 具体来说，服务端的表包含所有用户的数据，因此每条数据均有 `user_id` 字段声明数据归属的用户
    - 而客户端仅包含当前登录用户的数据，以提高数据安全、减少冗余  
    - 尽管每张表都有一个自增的 `id` 字段作为主键，但由于 `id` 的不同，客户端数据与服务端数据无法对应起来  
    - 因此，客户端的每条数据还有一个唯一的 UUID，这个 UUID 会与数据一同同步至服务端，实现数据的唯一性  
- 数据的写入和同步分离，客户端将数据同步至本地的 Room 数据库后，再由其他方法实现将数据同步至服务端  
    - 用户完成一个操作（如添加药品）后，无需等待网络请求即可进行其他操作，减少了用户的等待时间  
    - 数据同步可在后台进行，即使因为网络问题导致同步失败，数据也会在本地保存，并在下次启动时再次同步数据  
- 使用 Token 维护登录状态，每个用户最多可以拥有三个有效的 Token（即可以同时在三台设备上登录）  
    - 用户登录至 App 后，服务器返回 Token 并由 App 保存，后续请求均使用 Token 完成，不再需要传输密码或密码哈希，提高了安全性  
    - 每个 Token 均有有效期，超过有效期的 Token 会被认为失效，用户退出应用时会向服务器发送请求，服务器收到请求后也会使 Token 失效  
    - 如果有效的 Token 超过三个，新增 Token 时服务器会依次使最旧的 Token 失效，直到有效的 Token 少于或等于三个  
    - 每次启动 App 时，发送验证 Token 请求至服务器，同时完成网络检验  
    - 服务器每次收到请求后，会延长对应 Token 的有效期  
- 云同步首先判断数据更新时间，减少冗余请求  
    - 启动 App 时，客户端向服务端请求数据的上次更新时间（时间戳），并与客户端本地保存的上次更新时间进行对比，据此判断同步类型（向服务端上传数据或从服务端获取数据）  
    - 客户端数据库中包含 `sync_status` 字段，同步请求只会包含不为 `synced` 的数据并将其同步至服务器，减少 API 数据传输和服务端压力  
- 将界面文本存储至 `strings.xml` 支持本地化语言  
    - 在 Activity Layout 和 Kotlin 中分别使用 `@string/someText` 和 `context.getString(R.strings.someText)` 即可轻松获取本地化的文本

### 数据结构  
#### 服务端数据库  
- 关于数据库构造查询，请查看 [/server/table_creation_query](/server/table_creation_query/)
- `user` 表 - 用户  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | 用户 ID |
  | `username` | TEXT | 唯一、不为空 | 用户名，作为用户登录的凭证 |
  | `password_hash` | TEXT | 不为空 | 密码哈希，使用哈希加密以在传输过程中确保安全 |
  | `created_at` | TIMESTAMP | 不为空、默认为当前时间（北京时间） | 注册时间 |
  | `updated_at` | INTEGER | 不为空、默认为当前时间戳 | 服务端数据的上次修改时间 |
- `token` 表 - 登录凭据  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | Token ID |
  | `user_id` | INTEGER | 不为空、外键关联 `user.id` | Token 对应的用户 ID |
  | `token` | TEXT | 不为空、唯一 | 登录凭据 |
  | `expire_time` | TIMESTAMP | 不为空 | 凭据过期的时间（默认为当前时间 +7 天） |
- `medication` 表 - 药品  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | 数据 ID |
  | `user_id` | INTEGER | 不为空、外键关联 `user.id` | 归属的用户 ID |
  | `medication_name` | TEXT | 不为空 | 药品名 |
  | `patient_name` | TEXT | 不为空 | 需要用药的病人名 |
  | `dosage` | TEXT | 不为空 | 药物剂量，如 `2片` |
  | `remaining_amount` | TEXT | 不为空 | 药物余量 |
  | `frequency` | TEXT | 不为空 | 用药频率（如 `每日一次`） |
  | `week_mode` | TEXT | 不为空 | 提醒用药的星期（七位数字分别为周一至周日，`0` 代表当日提醒，`1` 代表当日不提醒，如 `1010000` 表示周一、周三提醒） |
  | `reminder_type` | TEXT | 不为空 | 提醒方式（三位数字分别为日历\[已弃用\]、闹钟和 App 通知，`0` 代表禁用，`1` 代表启用，如 `011` 代表启用闹钟和 App 通知，`000` 代表关闭所有提醒） |
  | `expiration_date` | TEXT | 不为空 | 药物过期时间，格式为 `yyyy-MM-dd`（如 `2024-12-19`） |
  | `client_uuid` | TEXT | 不为空 | 本条数据的 UUID，由客户端生成并上传 |
- `calendar_medication` 表 - 用药日期  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | 数据 ID |
  | `user_id` | INTEGER | 不为空、外键关联 `user.id` | 归属的用户 ID |
  | `medication_id` | INTEGER | 不为空，外键关联 `medication.id` | 关联的药品 ID |
  | `date` | DATE | 不为空 | 用药日期（如 `2024-12-19`） |
  | `client_uuid` | TEXT | 不为空 | 本条数据的 UUID，由客户端生成并上传 |
- `medication_time` 表 - 用药时间和状态  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | 数据 ID |
  | `user_id` | INTEGER | 不为空、外键关联 `user.id` | 归属的用户 ID |
  | `medication_id` | INTEGER | 不为空，外键关联 `medication.id` | 关联的药品 ID |
  | `date_id` | INTEGER | 不为空，外键关联 `calendar_medication.id` | 关联的日期 ID |
  | `status` | INTEGER | 不为空 | 用药状态（`-1` 初始化，`0` 漏打卡，`1` 已打卡，`2` 待打卡） |
  | `time` | TEXT | 不为空 | 用药时间（如 `08:08`） |
  | `client_uuid` | TEXT | 不为空 | 本条数据的 UUID，由客户端生成并上传 |
- `medication_box` 表 - 药箱  
  | 字段 | 类型 | 属性 | 描述 |
  | --- | --- | --- | --- |
  | `id` | INTEGER | 主键、自增 | 数据 ID |
  | `user_id` | INTEGER | 不为空、外键关联 `user.id` | 归属的用户 ID |
  | `box_name` | TEXT | 不为空 | 药箱名称 |
  | `box_type` | TEXT | 不为空 | 药箱类型 |
  | `applicable_people` | TEXT | 不为空 | 用药人 |
  | `medication_id` | INTEGER | 可为空，外键关联 `medication.id` | 关联的药品 ID |
  | `remark` | TEXT | 可为空 | 备注 |
  | `client_uuid` | TEXT | 不为空 | 本条数据的 UUID，由客户端生成并上传 |

#### 客户端数据库  
- 客户端数据库与服务端类似，除  
    - 客户端没有 `user` 和 `token` 表  
    - 客户端没有 `user_id` 字段  
    - 客户端 `client_uuid` 字段名称为 `uuid`  
    - 客户端增加了 `sync_status` 字段用于设定同步状态  
        - `synced`：已同步  
        - `created`：已创建，待同步  
        - `updated`：已更新，待同步  
        - `deleted`：已删除，待同步  

### API  
- Base URL：可在 App 中设置，默认为 `http://10.0.2.2:5000/`  
    - 这个 IP 地址在 Android Studio 的模拟器中用于代指代宿主机（运行模拟器和 Studio 的电脑）  
- 除 `/ping`、`/register`、`/login` 外，所有 API 均需要在请求头（Headers）中加入 `Authorization` 字段用于验证身份  
    - 如未包含，服务端返回 401 响应码和 `TOKEN_REQUIRED` 错误码  
    - 如 Token 无效（如已过期），服务端返回 401 响应码和 `INVALID_TOKEN` 错误码  
    - 示例
      ```
      Authorization: 56b707fc703b821bea2c1fcf6c34ca88f2549acefb62d3a5abf4ffd9786b0577
      ```
- 如果需要请求体，请求体需为 JSON 格式  
- 所有的 API 响应均为 JSON，格式如下  
  ```json
  {
      "code": 200,                    // HTTP 状态码
      "success": true,                // 请求是否成功
      "message": "Request success.",  // 描述信息
      "data": null,                   // 返回的具体数据（类型根据 API 决定）
      "error": {                      // 错误信息，如无错误为 null
          "code": "ERROR_CODE",       // 错误代码，字符串
          "description": "An error occurred."
      },                              // 错误描述，字符串
      "timestamp": 1734495716         // 时间戳
  }
  ```
- API 中如无特别说明，“响应”“成功”部分指的是响应体中的 `data` 字段是否包含内容

#### GET/POST `/ping`  
- 连接性检查，返回 `Pong`  
- 请求体：无  
- 响应：  
    - `message`：Pong  
    - `data`：无  

#### POST `/register`  
- 新用户注册  
- 请求体：  
  | 字段 | 描述 |
  | --- | --- |
  | `username` | 用户名 |
  | `passwordHash` | 密码哈希 |
- 响应  
    - 成功：无  
    - 失败  
        - `USERNAME_ALREADY_EXIST` - 用户名已存在
- 示例  
    - 请求  
      ```json
      {
          "username": "LKY",
          "passwordHash": "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"
      }
      ```
    - 响应  
        - 成功  
          ```json
          {
              "code": 200,
              "success": true,
              "message": "Register success",
              "data": null,
              "error": null,
              "timestamp": 1734541184
          }
          ```
        - 失败  
          ```json
          {
              "code": 404,
              "success": false,
              "message": null,
              "data": null,
              "error": {
                  "code": "USERNAME_ALREADY_EXIST",
                  "description": "The username LKY has already registered."
              }
          }
          ```
    - 后续请求与响应与此类似，不再提供示例  

#### POST `/login`  
- 账号登录  
- 方法：POST  
- 请求体  
  | 字段 | 类型 | 描述 |
  | --- | --- | --- |
  | `username` | 字符串 | 用户名 |
  | `passwordHash` | 字符串 | 密码哈希 |
- 响应  
    - 成功：JSON 对象  
      | 字段 | 类型 |描述 |
      | --- | --- | --- |
      | `token` | 字符串 | 用户凭证，用于其他请求 |
    - 失败  
        - `INVALID_USERNAME_OR_PASSWORD` - 无效的用户名或密码  

#### POST `/logout`  
- 账号登出  
    - 无论服务端结果是否成功，接收到服务端响应后客户端都应登出  
- 请求体：无  
- 响应  
    - 成功：无  
    - 失败  
        - `TOKEN_REQUIRED`：需要 Token  
        - `INVALID_TOKEN`：无效的 Token  

#### POST `/checkToken`  
- 验证 Token 是否有效  
    - 如有效，服务端会延长 Token 有效期  
    - 如无效，客户端应退出登录  
- 请求体：无  
- 响应  
    - 成功：无  
    - 失败  
        - `TOKEN_REQUIRED`：需要 Token  
        - `INVALID_TOKEN`：无效的 Token  

#### GET `/getLastUpdateTime`  
- 获取服务端数据的上次更新时间（时间戳）  
- 请求体：无  
- 响应  
    - 成功：JSON 对象  
      | 字段 | 类型 | 描述 |
      | --- | --- | --- |
      | `lastUpdateTime` | 整数 | 服务端数据的上次更新时间（时间戳） |

#### GET `/getMedicationData`  
- 获取用户的药品信息列表（`medication` 表中的数据）  
- 请求体：无  
- 响应：
    - 成功：JSON 对象
      | 字段 | 类型 | 描述 |
      | --- | --- | --- |
      | `medication` | JSON 对象的列表 | 药品信息列表 |
        - `medication` 列表信息如下  
          | 字段 | 类型 | 描述 |
          | --- | --- | --- |
          | `uuid` | 字符串 | 数据的 UUID，由客户端生成并上传 |
          | `medicationName` | 字符串 | 药品名 |
          | `patientName` | 字符串 | 需要用药的病人名 |
          | `dosage` | 字符串 | 剂量 |
          | `remainingAmount` | 字符串 | 剩余量 |
          | `frequency` | 字符串 | 用药频率 |
          | `weekMode` | 字符串 | 提醒用药的星期（七位数字分别为周一至周日，`0` 代表当日提醒，`1` 代表当日不提醒，如 `1010000` 表示周一、周三提醒） |
          | `reminderType` | 字符串 | 提醒方式（三位数字分别为日历[已弃用]、闹钟和 App 通知，`0` 代表禁用，`1` 代表启用，如 `011` 代表启用闹钟和 App 通知，`000` 代表关闭所有提醒） |
          | `expirationDate` | 字符串 | 药物过期时间，格式为 `yyyy-MM-dd`（如 `2024-12-19`） |

#### GET `/getCalendarMedicationData`  
- 获取用户的用药日期信息列表（`calendar_medication` 表中的数据）  
- 请求体：无  
- 响应：  
    - 成功：JSON 对象
      | 字段 | 类型 | 描述 |
      | --- | --- | --- |
      | `calendarMedication` | JSON 对象的列表 | 用药日期信息列表 |
        - `calendarMedication` 列表信息如下  
          | 字段 | 类型 | 描述 |
          | --- | --- | --- |
          | `uuid` | 字符串 | 数据的 UUID，由客户端生成并上传 |
          | `medicationUuid` | 字符串 | 关联的药品 UUID |
          | `date` | 字符串 | 不为空 | 用药日期（如 `2024-12-19`） |

#### GET `/getMedicationTimeData`  
- 获取用户的用药时间信息列表（`medication_time` 表中的数据）  
- 请求体：无  
- 响应  
    - 成功：JSON 对象  
      | 字段 | 类型 | 描述 |
      | --- | --- | --- |
      | `medicationTime` | JSON 对象的列表 | 用药时间信息列表 |
        - `medicationTime` 列表信息如下  
          | 字段 | 类型 | 描述 |
          | --- | --- | --- |
          | `uuid` | 字符串 | 数据的 UUID，由客户端生成并上传 |
          | `medicationUuid` | 字符串 | 关联的药品 UUID |
          | `dateUuid` | 字符串 | 关联的日期 UUID |
          | `status` | 整数 | 用药状态（`-1` 初始化，`0` 漏打卡，`1` 已打卡，`2` 待打卡） |
          | `time` | 字符串 | 用药时间（如 `13:32`） |

#### GET `/getMedicineBoxData`  
- 获取用户的药箱信息列表（`medicine_box` 表中的数据）  
- 请求体：无  
- 响应  
    - 成功：JSON 对象  
      | 字段 | 类型 | 描述 |
      | --- | --- | --- |
      | `medicineBox` | JSON 对象的列表 | 药箱信息列表 |
        - `medicineBox` 列表信息如下  
          | 字段 | 类型 | 描述 |
          | --- | --- | --- |
          | `uuid` | 字符串 | 数据的 UUID，由客户端生成并上传 |
          | `boxName` | 字符串 | 药箱名 |
          | `boxType` | 字符串 | 药箱类型 |
          | `applicablePeople` | 字符串 | 用药人 |
          | `medicationUuid` | 字符串 / `null` | 关联的药品 UUID |
          | `remark` | 字符串 / `null` | 备注 |

#### DELETE `/clearData`  
- 清除服务端数据库中该用户的数据  
- 请求体：无  
- 响应  
    - 成功：无  

#### POST `/insertMedicationData`  
- 将药品信息数据插入至服务器中的 `medication` 表  
- 请求：与 [GET `/getMedicationData`](#get-getmedicationdata) 的响应格式相同  
- 响应  
    - 成功：无  

#### POST `/insertCalendarMedicationData`  
- 将药品信息数据插入至服务器中的 `calendar_medication` 表  
- 请求：与 [GET `/getCalendarMedicationData`](#get-getcalendarmedicationdata) 的响应格式相同  
- 响应  
    - 成功：无  

#### POST `/insertMedicineBoxData`  
- 将药品信息数据插入至服务器中的 `medicine_box` 表  
- 请求：与 [GET `/getMedicationTimeData`](#get-getmedicationtimedata) 的响应格式相同  
- 响应  
    - 成功：无  

#### POST `/insertMedicationData`  
- 将药品信息数据插入至服务器中的 `medication` 表  
- 请求：与 [GET `/getMedicineBoxData`](#get-getmedicineboxdata) 的响应格式相同  
- 响应  
    - 成功：无  