# jsQrEdit.edit

## Definition

```js.js
function jsQrEdit.edit(
	${qrConFilePathString},
	${broadcastIntentMapStrString},
) -> runEdit
```

- The `run` prefix annotation is a process annotation
## Definition by js action

```js.js
var=runEdit
	?func=jsQrEdit.edit
	?args=
		&qrConFilePath=${String}
		&broadcastIntentMapStr=${String}
```

- [js action](#) is annotation-oriented language based on javascript in `CommandClick`

- The `run` prefix definition on `var` is a process annotation, not a variable definition

## Src

-> [jsQrEdit.edit](https://github.com/puutaro/CommandClick/blob/master/app/src/main/java/com/puutaro/commandclick/fragment_lib/terminal_fragment/js_interface/qr/JsQrEdit.kt#L56)


