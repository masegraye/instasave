# instasave

## Build unpacked extension

Requires lein

Requires rsync

```
$ ./script/extension
```

## Test unpacked extension

1. Open your Chrome preferences
1. Click Extensions
1. Enable **Developer mode** by clicking the checkbox in the upper-right corner.
1. Click **Load unpacked extension...**
1. Navigate to `[project root]/target/extension-final/` and click Select.

You should now have the **instasave** extension loaded in your browser. Click **background.html** after the **Inspect views:** label. After navigating to the **Console** tab, you should see "Hello, world" followed by "foo".
