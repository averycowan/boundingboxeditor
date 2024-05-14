# Bounding Box Editor

This is a lightweight tool to view images, and draw bounding boxes on them. It is written in java, but relies on POSIX paths to open files.

# How to use

The workflow is to run each labeling task very quick and simple but makes several assumptions in the setup.

## Setup

1. Decide which label categories you would like to have
  - Put each label name on a new line in `~/Pictures/BoundingBoxEditor/label_names.txt`
2. Place source images in the folder `~/Pictures/BoundingBoxEditor/images/`
  - Each image should be called `image_title.jpg`
3. Prepare the list of tasks
  - Each task is a combination of an image and a label category
  - Format each of the tasks as the string `image_title+label_category`
4. Create the `~/Pictures/BoundingBoxEditor/labels/` directory for results
  - Outputs will appear here as `image_title+label_category.csv`
  - Each row is one bounding box
  - The columns are `xmin,ymin,xmax,ymax,label`
5. Launch the application

## How to label an image

1. Copy a task string to the clipboard
2. In the application press Cmd-O to open
3. With the mouse, draw as many bounding boxes as you would like
4. If you make a mistake press Cmd-Z to undo the most recent drawing
5. When you're done press Cmd-S to save the boxes

# Keybinds

I haven't yet added a menu bar. It's all keybinds for now, which I find to be faster for many tasks.

| Key | Action |
|-----|------|
| ⌘-O | Open |
| ⌘-S | Save |
| ⌘-Z | Undo |
