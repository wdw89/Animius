# [S1] 动漫源切换统一改造

## [S2] 问题

- Week 和 Home 页面切换源功能不统一：Week 页面用 AlertDialog + RadioButton，Home 页面无切换功能
- 切换源后只有当前页面刷新，其他页面不同步
- Week 页面切换源需要点击确认按钮，操作繁琐
- Home 页面站源文本无焦点样式，不能使用 D-pad 操作

## [S3] 改动的文件

| 文件 | 改动 |
|------|------|
| `util/SourceHolder.kt` | `isSourceChanged` 改为 `mutableStateOf(0)` 计数器 |
| `screen/week/WeekScreen.kt` | `SourceSwitchDialog` 改为 public，添加 `onSourceChanged` 回调，移除确认/取消按钮 |
| `screen/home/HomeScreen.kt` | 站源文本改为 `Surface(onClick)` + `SourceSwitchDialog`，添加 `focusGroup()` |
| `screen/home/HomeScreen.kt` | 站源使用 `surfaceVariant.copy(alpha = 0.6f)` 焦点背景色 |

## [S4] 关键实现

1. **跨页面刷新**: `SourceHolder.isSourceChanged` 用 `mutableStateOf(0)` 递增计数器代替布尔值
2. **Week 页面**: `SourceSwitchDialog` 改为 public，选中 RadioButton 立即切换并刷新
3. **Home 页面**: 站源名称用 `Surface(onClick) + RoundedCornerShape(20.dp)` + `sourceSwitchDialog` 弹出相同源选择列表
4. **焦点背景**: `onFocusChanged` + `surfaceVariant.copy(alpha = 0.6f)` 统一圆角矩形效果
