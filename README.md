# MediCare 呵药  
- **Medi**cine + **Care** → **MediCare**  
- 智能药品管理 & 提醒 App  
- 2024 年秋季阿伯丁学院 第 6 组  
### 项目目录  
- [**/doc**](/doc/) - 项目文档  
    - [**/01**](/doc/01%20商业计划书.md) - **商业计划书 App Business Plan**  
    - [**/02**](/doc/02%20MediCare%20设计方案汇报.pptx) - 设计方案汇报（PPT） 
    - [**/03**](/doc/03%20MediCare%20设计方案汇报%20PDF版.pdf) - 设计方案汇报（PDF）  
    - [**/04**](/doc/04%20MediCare%20UI设计汇报.pptx) - UI 设计汇报（PPT）  
    - [**/05**](/doc/05%20MediCare%20UI设计汇报%20PDF版.pdf) - UI 设计汇报（PDF）  
    - [**/06**](/doc/06%20MediCare%20UI演示.mp4) - UI 演示视频  
    - [**/07**](/doc/07%20MediCare%20界面流程图.png) - 界面流程图  
    - [**/08**](/doc/08%20MediCare%20界面流程图（drawio存档）.xml) - 界面流程图（[draw.io](https://app.diagrams.net/) 存档）  
    - [**/09**](/doc/09%20MediCare%20最终汇报.pptx) - 最终汇报（PPT）  
    - [**/10**](/doc/10%20MediCare%20最终汇报%20PDF%20版.pdf) - 最终汇报（PDF）  
    - [**/11**](/doc/11%20MediCare%20技术细节.md) - 技术细节（数据结构和接口文档）  
    - [**/12**](/doc/12%20Software%20Design%20Document.docx) - 软件设计报告 Software Design Document（Word 文档）  
    - [**/13**](/doc/13%20Software%20Design%20Document.pdf) - 软件设计报告（PDF）  
    - [**/附**](/doc//附%20-%20CalendarPage%20开发问题与解决.md) - CalendarPage 开发问题与解决（自本 README.md 拆分）  
- [**/server**](/server/) - App 服务器，使用 Python 实现，服务端目录介绍详见 [/server/README.md](/server/README.md)  
- 其余目录和文件 - Android 项目  

#### Android 项目主要目录
[**/app/src/main**](/app/src/main/)  
- [**/java/com/appdev/medicare**](/app/src/main/java/com/appdev/medicare/)  
    - [**\*.kt**](/app/src/main/java/com/appdev/medicare/) - 各 Activity 和 Fragment 的功能实现  
    - [**/api**](/app/src/main/java/com/appdev/medicare/api/) - 网络请求相关功能  
    - [**/model**](/app/src/main/java/com/appdev/medicare/model/) - 对象文件  
    - [**/receiver**](/app/src/main/java/com/appdev/medicare/receiver/) - 通知相关  
    - [**/room**](/app/src/main/java/com/appdev/medicare/room/) - Room 数据库  
        - [**/dao**](/app/src/main/java/com/appdev/medicare/room/dao/) - 数据访问对象（<u>**D**</u>ata <u>**A**</u>ccess <u>**O**</u>bject）  
        - [**/entity**](/app/src/main/java/com/appdev/medicare/room/entity/) - Room 数据库实体  
    - [**/utils**](/app/src/main/java/com/appdev/medicare/utils/) - 工具类、对象和函数  
- [**/res**](/app/src/main/res/) - 资源文件目录  
    - [**/drawable**](/app/src/main/res/drawable/) - XML 图标资源  
    - [**/layout**](/app/src/main/res/layout/) - Activity 布局文件  
    - [**/values**](/app/src/main/res/values/) - 颜色、文本资源值（包含颜色、字符串等）  
    - [**/values-zh**](/app/src/main/res/values-zh/) - 中文（中国）本地化资源值  

### APK 安装包  
- **详见 [Releases](https://github.com/android-app-development-course/2024-Autumn-Aberdeen-6-MediCare/releases)**  