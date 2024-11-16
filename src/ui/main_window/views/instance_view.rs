use adw::{Toast, ToastOverlay};
use async_channel::Sender;
use gtk::{
    glib::{self, clone},
    prelude::*,
    Align, Box, Button, Image, Label, Orientation, Widget,
};

use crate::{
    config::instance_config::{InstanceConfigManager, InstanceConfigStruct},
    minecraft::Minecraft,
};

pub struct InstanceView {}

impl InstanceView {
    pub fn new(
        toast_overlay: ToastOverlay,
        instance_config_struct: InstanceConfigStruct,
        sender: Sender<String>,
    ) -> impl IsA<Widget> {
        let icon = Image::new();
        icon.set_from_file(Some(&instance_config_struct.image_path));
        icon.set_size_request(192, 192);
        icon.set_icon_size(gtk::IconSize::Inherit);
        icon.set_halign(gtk::Align::Start);
        icon.set_valign(gtk::Align::Start);
        icon.set_css_classes(&["card", "rounded"]);

        let name = Label::new(Some(instance_config_struct.name.as_str()));
        name.set_css_classes(&["title-1"]);

        let version = Label::new(Some(instance_config_struct.version.as_str()));
        version.set_css_classes(&["title-4"]);
        version.set_halign(Align::Start);

        let label_container = Box::builder()
            .orientation(Orientation::Vertical)
            .spacing(2)
            .halign(Align::Start)
            .margin_start(6)
            .build();
        label_container.append(&name);
        label_container.append(&version);

        let primary_button = Button::builder()
            .label(if instance_config_struct.downloaded {
                "Play"
            } else {
                "Download"
            })
            .css_classes(["pill", "suggested-action"])
            .build();

        let primary_button_clone = primary_button.clone();
        let toast_overlay_clone = toast_overlay.clone();
        let instance_config_struct_clone = instance_config_struct.clone();
        let sender_clone = sender.clone();
        primary_button.connect_clicked(move |_| {
            let toast_overlay = &toast_overlay_clone;
            let instance_config_struct = &instance_config_struct_clone;
            let sender = &sender_clone;

            let minecraft = Minecraft::new(instance_config_struct.slug.as_str());

            let (toast_sender, toast_receiver) = async_channel::bounded(1);

            if instance_config_struct.downloaded {
                primary_button_clone.set_label("Launching");

                let toast_sender_clone = toast_sender.clone();
                let instance_config_struct_clone = instance_config_struct.clone();
                gtk::gio::spawn_blocking(move || {
                    let result = minecraft.launch(true);
                    if let Err(_) = result {
                        toast_sender_clone
                            .send_blocking("Failed to launch the game!".to_string())
                            .expect("Failed to send message");
                    } else {
                        toast_sender_clone
                            .send_blocking(format!(
                                "Launched instance {}",
                                instance_config_struct_clone.name
                            ))
                            .expect("Failed to send message");
                    }
                });

                primary_button_clone.set_label("Play");
            } else {
                primary_button_clone.set_label("Downloading");

                let sender_clone = sender.clone();
                let instance_config_struct_clone = instance_config_struct.clone();
                let toast_sender_clone = toast_sender.clone();
                gtk::gio::spawn_blocking(move || {
                    let result = minecraft.download(instance_config_struct_clone.version.as_str());
                    if let Err(_) = result {
                        toast_sender_clone
                            .send_blocking(format!(
                                "Failed to download {}",
                                instance_config_struct_clone.name
                            ))
                            .expect("Failed to send message");
                    }

                    toast_sender_clone
                        .send_blocking(format!(
                            "Downloaded instance {}",
                            instance_config_struct_clone.name
                        ))
                        .expect("Failed to send message");

                    sender_clone
                        .send_blocking("refresh".to_string())
                        .expect("Failed to send message");
                });
            }

            glib::spawn_future_local(clone!(
                #[weak]
                toast_overlay,
                async move {
                    while let Ok(message) = toast_receiver.recv().await {
                        toast_overlay.add_toast(Toast::new(&message));
                    }
                }
            ));
        });

        // TODO:add a popup for editing the instance
        let edit_button = Button::builder()
            .label("Edit")
            .css_classes(["pill"])
            .build();

        let delete_button = Button::builder()
            .label("Delete")
            .css_classes(["pill", "destructive-action"])
            .build();

        let delete_button_clone = delete_button.clone();
        let toast_overlay_clone = toast_overlay.clone();
        let instance_config_struct_clone = instance_config_struct.clone();
        let sender_clone = sender.clone();
        delete_button.connect_clicked(move |_| {
            let toast_overlay = &toast_overlay_clone;
            let instance_config_struct = &instance_config_struct_clone;
            let sender = &sender_clone;

            let (toast_sender, toast_receiver) = async_channel::bounded(1);

            delete_button_clone.set_label("Deleting");

            let instance_config_struct_clone = instance_config_struct.clone();
            let toast_sender_clone = toast_sender.clone();
            let sender_clone = sender.clone();
            gtk::gio::spawn_blocking(move || {
                let instance_config =
                    InstanceConfigManager::new(instance_config_struct_clone.slug.as_str());
                let result = instance_config.delete();
                if let Err(error) = result {
                    toast_sender_clone
                        .send_blocking(format!(
                            "Failed to delete instance {}\n\n{error}",
                            instance_config_struct_clone.name
                        ))
                        .expect("Failed to send message");
                }

                toast_sender_clone
                    .send_blocking(format!(
                        "Deleted instance {}",
                        instance_config_struct_clone.name
                    ))
                    .expect("Failed to send message");

                sender_clone
                    .send_blocking("refresh".to_string())
                    .expect("Failed to send message");
            });

            glib::spawn_future_local(clone!(
                #[weak]
                toast_overlay,
                async move {
                    while let Ok(message) = toast_receiver.recv().await {
                        toast_overlay.add_toast(Toast::new(&message));
                    }
                }
            ));
        });

        let button_container = Box::new(Orientation::Horizontal, 12);
        button_container.append(&primary_button);
        button_container.append(&edit_button);
        button_container.append(&delete_button);

        let container = gtk::Box::builder()
            .orientation(Orientation::Vertical)
            .spacing(12)
            .margin_start(12)
            .margin_end(12)
            .margin_top(12)
            .margin_bottom(12)
            .hexpand(false)
            .vexpand(false)
            .build();
        container.append(&icon);
        container.append(&label_container);
        container.append(&button_container);

        container
    }
}
