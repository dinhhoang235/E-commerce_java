-- Seed Apple Products
-- Run this manually when you want test data:
-- mysql -u root -p ecommerce_db < db/seed/seed_apple_products.sql

-- Categories
INSERT INTO categories (name, slug, description, is_active, sort_order, product_count, created_at, updated_at) VALUES
('iPhone', 'iphone', 'iPhone – powerful, intuitive, and beautifully designed.', b'1', 1, 0, NOW(6), NOW(6)),
('iPad', 'ipad', 'iPad – creative, productive, and versatile.', b'1', 2, 0, NOW(6), NOW(6)),
('Macbook', 'macbook', 'Macbook – powerful performance, stunning design.', b'1', 3, 0, NOW(6), NOW(6)),
('Watch', 'watch', 'Apple Watch – health, fitness, and connection.', b'1', 4, 0, NOW(6), NOW(6)),
('AirPods & Audio', 'airpods-audio', 'AirPods – wireless, effortless, immersive.', b'1', 5, 0, NOW(6), NOW(6)),
('Accessories', 'accessories', 'Apple Accessories – designed for your devices.', b'1', 6, 0, NOW(6), NOW(6));

-- Product Colors
INSERT INTO product_colors (name, hex_code) VALUES
('Space Black', '#1d1d1d'),
('Silver', '#e3e4e6'),
('Gold', '#fad7bd'),
('Space Gray', '#6c6c70'),
('Midnight', '#2e3641'),
('Starlight', '#f8f6ee'),
('Blue', '#a6c0d4'),
('Purple', '#d5c9df'),
('Pink', '#f5d5d8'),
('Green', '#d0dfd3'),
('Product Red', '#e73c34'),
('White', '#ffffff'),
('Deep Purple', '#574f6b'),
('Yellow', '#f7e05e'),
('Orange', '#f5a623');

-- Products
-- 1: iPhone 16 Pro Max
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(1, 'iPhone 16 Pro Max', 1, NULL, 4.8, 2341, 'New',
'The most powerful iPhone ever. A18 Pro chip, 48MP Fusion camera, and stunning titanium design.',
'iPhone 16 Pro Max delivers an extraordinary experience with the A18 Pro chip, a 48MP Fusion camera system with 5x optical zoom, and a durable titanium design. The Super Retina XDR display with ProMotion technology brings content to life, while all-day battery life keeps you going. Powered by Apple Intelligence, it is the ultimate iPhone.',
'["Titanium design — strong, light, and premium","A18 Pro chip with 6-core GPU","48MP Fusion camera with 5x optical zoom","Super Retina XDR display with ProMotion","All-day battery life up to 33 hours","USB-C with USB 3 support","Apple Intelligence built-in"]', NOW(6), NOW(6));

-- 2: iPhone 16 Pro
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(2, 'iPhone 16 Pro', 1, NULL, 4.7, 1823, 'New',
'A18 Pro chip. 48MP Fusion camera. Pro motion capture. All in a titanium design.',
'iPhone 16 Pro combines power and elegance with the A18 Pro chip, a professional camera system, and a lightweight titanium design. Capture stunning photos and videos, enjoy immersive gaming, and stay productive with Apple Intelligence. The perfect balance of performance and portability.',
'["Titanium design","A18 Pro chip with 6-core GPU","48MP Fusion camera with 3x optical zoom","Super Retina XDR display with ProMotion","Up to 27 hours video playback","USB-C with USB 3 support","Apple Intelligence built-in"]', NOW(6), NOW(6));

-- 3: iPhone 16
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(3, 'iPhone 16', 1, NULL, 4.6, 3210, 'New',
'A18 chip. 48MP camera. Camera Control. Designed for Apple Intelligence.',
'iPhone 16 brings the power of the A18 chip and a 48MP camera system to everyone. With Camera Control, vibrant colors, and Apple Intelligence, iPhone 16 is designed to help you capture memories, stay connected, and get things done. Available in six stunning colors.',
'["A18 chip with 5-core GPU","48MP Fusion camera with 2x optical zoom","Camera Control for quick adjustments","Super Retina XDR display","Up to 22 hours video playback","USB-C","Apple Intelligence built-in"]', NOW(6), NOW(6));

-- 4: iPhone 16 Plus
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(4, 'iPhone 16 Plus', 1, NULL, 4.5, 1256, 'New',
'Big screen. A18 chip. 48MP camera. The ultimate plus-size iPhone experience.',
'iPhone 16 Plus offers everything you love about iPhone 16 on a larger 6.7-inch display. With the A18 chip, a 48MP camera system, and incredible battery life, iPhone 16 Plus is perfect for those who want more screen and more power.',
'["Larger 6.7-inch Super Retina XDR display","A18 chip with 5-core GPU","48MP Fusion camera with 2x optical zoom","Camera Control","Up to 27 hours video playback","USB-C","Apple Intelligence built-in"]', NOW(6), NOW(6));

-- 5: MacBook Pro 16
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(5, 'MacBook Pro 16"', 3, NULL, 4.9, 4512, 'Best Seller',
'Supercharged by M4 Max or M4 Ultra. Stunning 16-inch Liquid Retina XDR display.',
'MacBook Pro 16" is the ultimate pro laptop, powered by M4 Max or M4 Ultra chips. With a stunning 16-inch Liquid Retina XDR display with ProMotion, up to 22 hours of battery life, and a advanced thermal system, it delivers desktop-class performance for the most demanding workflows.',
'["M4 Max or M4 Ultra chip","16.2-inch Liquid Retina XDR display with ProMotion","Up to 22 hours battery life","36GB to 128GB unified memory","Thunderbolt 5 ports","Studio-quality three-mic array","6-speaker sound system with Spatial Audio"]', NOW(6), NOW(6));

-- 6: MacBook Pro 14
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(6, 'MacBook Pro 14"', 3, NULL, 4.8, 3890, 'Best Seller',
'Powered by M4 Pro or M4 Max. 14-inch Liquid Retina XDR display. Pro performance in a portable design.',
'MacBook Pro 14" delivers exceptional performance in a compact, portable design. With M4 Pro or M4 Max chips, a brilliant 14-inch Liquid Retina XDR display, and professional connectivity, it is the perfect choice for pros on the go.',
'["M4 Pro or M4 Max chip","14.2-inch Liquid Retina XDR display with ProMotion","Up to 18 hours battery life","Thunderbolt 5 ports","Studio-quality three-mic array","6-speaker sound system with Spatial Audio","Advanced thermal management"]', NOW(6), NOW(6));

-- 7: MacBook Air M4
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(7, 'MacBook Air M4', 3, NULL, 4.7, 2105, 'New',
'Superlight. Superpowered. M4 chip. 13.6-inch Liquid Retina display. Four stunning colors.',
'MacBook Air M4 is the worlds most popular laptop, now supercharged by the M4 chip. With a stunning 13.6-inch Liquid Retina display, 1080p FaceTime HD camera, and all-day battery life, it is incredibly thin, quiet, and powerful. Available in four beautiful colors.',
'["M4 chip with 10-core GPU","13.6-inch Liquid Retina display","Up to 18 hours battery life","16GB unified memory","1080p FaceTime HD camera","MagSafe charging","Thin and light design just 1.24 kg"]', NOW(6), NOW(6));

-- 8: iMac 24
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(8, 'iMac 24"', 3, NULL, 4.6, 1847, NULL,
'Vibrant 24-inch display. M4 chip. Seven stunning colors. The all-in-one for everyone.',
'iMac 24" brings the M4 chip to the iconic all-in-one design. With a vibrant 24-inch 4.5K Retina display, advanced camera and audio, and seven gorgeous colors, iMac is the ultimate desktop experience for creativity, productivity, and entertainment.',
'["M4 chip with 10-core GPU","24-inch 4.5K Retina display","Up to 16GB unified memory","1080p FaceTime HD camera","6-speaker sound system","Touch ID on Magic Keyboard","Seven vibrant colors"]', NOW(6), NOW(6));

-- 9: iPad Pro M4 13
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(9, 'iPad Pro M4 13"', 2, NULL, 4.8, 1567, 'New',
'The ultimate iPad experience. M4 chip. Ultra Retina XDR display. Ultra slim design.',
'iPad Pro M4 13" is the thinnest and most powerful iPad ever. The M4 chip delivers incredible performance, the Ultra Retina XDR display is stunning, and the all-new design is remarkably thin and light. With Apple Pencil Pro support and Magic Keyboard, it is the ultimate creative and productivity tool.',
'["M4 chip with 10-core GPU","13-inch Ultra Retina XDR display","Nanotexture glass option","Thin and light design at just 5.1mm","Apple Pencil Pro support","Face ID","USB-C with Thunderbolt 4"]', NOW(6), NOW(6));

-- 10: iPad Pro M4 11
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(10, 'iPad Pro M4 11"', 2, NULL, 4.7, 1234, 'New',
'M4 chip. Ultra Retina XDR display. Powerful and portable.',
'iPad Pro M4 11" packs the incredible power of the M4 chip into an ultra-portable 11-inch design. Perfect for creative professionals and students who need pro performance in a compact form factor.',
'["M4 chip with 10-core GPU","11-inch Ultra Retina XDR display","Nanotexture glass option","Ultra thin at just 5.3mm","Apple Pencil Pro support","Face ID","USB-C with Thunderbolt 4"]', NOW(6), NOW(6));

-- 11: iPad Air M2 13
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(11, 'iPad Air M2 13"', 2, NULL, 4.6, 2145, NULL,
'Supercharged by M2. Large 13-inch Liquid Retina display. Striking colors.',
'iPad Air M2 13" delivers incredible performance and versatility with the M2 chip and a spacious 13-inch Liquid Retina display. With support for Apple Pencil Pro and Magic Keyboard, it is perfect for students, creators, and professionals.',
'["M2 chip","13-inch Liquid Retina display","Landscape 12MP Ultra Wide camera","Apple Pencil Pro support","Touch ID","USB-C","Available in four colors"]', NOW(6), NOW(6));

-- 12: iPad 10th Gen
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(12, 'iPad 10th Gen', 2, NULL, 4.5, 3876, NULL,
'A14 Bionic chip. 10.9-inch Liquid Retina display. Colorful and capable.',
'iPad 10th Gen offers the full iPad experience at an incredible value. With the A14 Bionic chip, a beautiful 10.9-inch Liquid Retina display, and support for Apple Pencil and Magic Keyboard Folio, it is perfect for everyday tasks, learning, and entertainment.',
'["A14 Bionic chip","10.9-inch Liquid Retina display","12MP Wide camera","Landscape 12MP Ultra Wide front camera","Touch ID","USB-C","Four fun colors"]', NOW(6), NOW(6));

-- 13: Apple Watch Ultra 3
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(13, 'Apple Watch Ultra 3', 4, NULL, 4.9, 892, 'New',
'The most rugged and capable Apple Watch ever. 49mm titanium case. Precision dual-frequency GPS.',
'Apple Watch Ultra 3 is the ultimate sports and adventure watch. With a rugged 49mm titanium case, precision dual-frequency GPS, and up to 36 hours of battery life, it is built for endurance athletes, explorers, and anyone who pushes beyond limits.',
'["49mm titanium case","Precision dual-frequency GPS","Up to 36 hours battery life","3000-nit display","Action button","Siren and strobe light","Depth gauge and water resistance to 100m"]', NOW(6), NOW(6));

-- 14: Apple Watch Series 10
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(14, 'Apple Watch Series 10', 4, NULL, 4.7, 3451, 'New',
'Biggest display ever. Thinnest design yet. Advanced health features.',
'Apple Watch Series 10 features the largest and most advanced display ever in an Apple Watch, in the thinnest design. With health monitoring capabilities including sleep apnea detection, temperature sensing, and ECG, it is the ultimate health companion.',
'["Largest display ever on Apple Watch","Thinnest design at 9.7mm","Sleep apnea detection","Temperature sensing","ECG and blood oxygen","Fast charging","Water resistant to 50m"]', NOW(6), NOW(6));

-- 15: AirPods Pro 3
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(15, 'AirPods Pro 3', 5, NULL, 4.8, 5678, 'New',
'Adaptive Audio. Active Noise Cancellation. Personalized Spatial Audio.',
'AirPods Pro 3 deliver an extraordinary audio experience with Adaptive Audio that seamlessly blends Active Noise Cancellation and Transparency mode. With Personalized Spatial Audio, improved battery life, and a new Find My experience, they are the most advanced AirPods ever.',
'["Adaptive Audio mode","Active Noise Cancellation","Personalized Spatial Audio","H2 chip","Up to 6 hours listening time","USB-C MagSafe charging case","Find My with precision finding"]', NOW(6), NOW(6));

-- 16: AirPods Max 2
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(16, 'AirPods Max 2', 5, NULL, 4.6, 2345, NULL,
'Over-ear headphones with H2 chip. Active Noise Cancellation. Stunning sound.',
'AirPods Max 2 combine the H2 chip with over-ear design for exceptional audio quality. With Active Noise Cancellation, Personalized Spatial Audio, and a breathable knit mesh canopy, they deliver immersive sound in supreme comfort.',
'["H2 chip","Active Noise Cancellation","Personalized Spatial Audio","Over-ear design with breathable knit mesh","Up to 20 hours listening time","USB-C charging","Five stunning colors"]', NOW(6), NOW(6));

-- 17: AirPods 4
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(17, 'AirPods 4', 5, NULL, 4.5, 4321, NULL,
'Redesigned for comfort and sound. H2 chip. USB-C. Personalized Spatial Audio.',
'AirPods 4 offer a redesigned shape that fits more comfortably than ever. With the H2 chip, Personalized Spatial Audio, and USB-C charging, they deliver great sound and seamless integration with all your Apple devices.',
'["H2 chip","Redesigned comfort fit","Personalized Spatial Audio","Up to 5 hours listening time","USB-C charging case","IP54 dust and sweat resistant","Seamless iCloud integration"]', NOW(6), NOW(6));

-- 18: Mac Mini M4
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(18, 'Mac Mini M4', 3, NULL, 4.7, 789, 'New',
'M4 and M4 Pro. Thunderbolt 5. Compact design. Enormous performance.',
'Mac Mini M4 redefines what a desktop can do. With the powerful M4 or M4 Pro chip, Thunderbolt 5 connectivity, and support for multiple displays, this compact powerhouse fits anywhere and handles everything.',
'["M4 or M4 Pro chip","Thunderbolt 5 ports","Up to 64GB unified memory","Three display support","Compact 5x5 inch design","Front and rear USB-C ports","10Gb Ethernet option"]', NOW(6), NOW(6));

-- 19: Mac Studio M4 Max
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(19, 'Mac Studio M4 Max', 3, NULL, 4.8, 567, NULL,
'Breakthrough performance. Extensive connectivity. Compact design.',
'Mac Studio M4 Max delivers extraordinary performance in a compact form. With M4 Max or M4 Ultra chips, extensive connectivity including Thunderbolt 5, and a revolutionary thermal design, it is the ultimate creative workstation.',
'["M4 Max or M4 Ultra chip","Up to 128GB unified memory","Thunderbolt 5 ports","HDMI 2.1","10Gb Ethernet","Compact 7.7-inch square design","Supports up to 8 displays"]', NOW(6), NOW(6));

-- 20: Mac Pro
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(20, 'Mac Pro', 3, NULL, 4.9, 345, NULL,
'Supreme performance. PCIe expansion. The most powerful Mac ever.',
'Mac Pro combines the incredible performance of Apple silicon with the versatility of PCIe expansion. With M4 Ultra chip, up to 192GB of unified memory, and seven PCIe slots, it is built for professionals who need the absolute maximum performance.',
'["M4 Ultra chip with 80-core GPU","Up to 192GB unified memory","Seven PCIe expansion slots","Thunderbolt 5 ports","Three display support","Stainless steel frame with handles","1.5TB/s memory bandwidth"]', NOW(6), NOW(6));

-- 21: iPhone 15 Pro Max
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(21, 'iPhone 15 Pro Max', 1, NULL, 4.7, 5678, 'Best Seller',
'A17 Pro chip. Titanium design. 48MP camera system with 5x optical zoom.',
'iPhone 15 Pro Max features a strong and lightweight titanium design, the A17 Pro chip for next-level performance, and a powerful 48MP camera system with 5x optical zoom. The first iPhone with USB-C brings a new level of convenience.',
'["Titanium design","A17 Pro chip with 6-core GPU","48MP camera with 5x optical zoom","Super Retina XDR display with ProMotion","Up to 29 hours video playback","USB-C with USB 3 support","Action button"]', NOW(6), NOW(6));

-- 22: iPhone 15
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(22, 'iPhone 15', 1, NULL, 4.5, 8241, 'Best Seller',
'A16 Bionic chip. 48MP camera. Dynamic Island. USB-C.',
'iPhone 15 brings the Dynamic Island to the lineup, along with a powerful 48MP camera system, the A16 Bionic chip, and USB-C. Available in five beautiful colors, it is designed to make every moment better.',
'["A16 Bionic chip","48MP main camera with 2x optical zoom","Dynamic Island","Super Retina XDR display","Up to 20 hours video playback","USB-C","Satellite connectivity"]', NOW(6), NOW(6));

-- 23: iPhone SE 3
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(23, 'iPhone SE 3', 1, NULL, 4.3, 4567, NULL,
'A15 Bionic chip. 12MP camera. Touch ID. Great value.',
'iPhone SE 3 packs the powerful A15 Bionic chip into a compact design with Touch ID. With a 12MP camera system, 5G connectivity, and incredible battery life, it is the most affordable iPhone.',
'["A15 Bionic chip","12MP camera with Portrait mode","Touch ID","4.7-inch Retina HD display","5G connectivity","Up to 15 hours video playback","Glass and aluminum design"]', NOW(6), NOW(6));

-- 24: iPad Mini 7
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(24, 'iPad Mini 7', 2, NULL, 4.6, 1890, 'New',
'A17 Pro chip. 8.3-inch Liquid Retina display. Ultra-portable design.',
'iPad Mini 7 is powered by the A17 Pro chip, making it incredibly powerful in a compact 8.3-inch design. Perfect for reading, note-taking, and gaming on the go. Supports Apple Pencil Pro.',
'["A17 Pro chip","8.3-inch Liquid Retina display","Apple Pencil Pro support","12MP Wide camera","Landscape 12MP Ultra Wide front camera","Touch ID","USB-C"]', NOW(6), NOW(6));

-- 25: iPad Air M2 11"
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(25, 'iPad Air M2 11"', 2, NULL, 4.5, 2340, NULL,
'Supercharged by M2. 11-inch Liquid Retina display. Perfect balance of power and portability.',
'iPad Air M2 11" brings the M2 chip to a compact 11-inch form factor. With a Liquid Retina display, Apple Pencil Pro support, and all-day battery life, it is ideal for students, creators, and professionals.',
'["M2 chip","11-inch Liquid Retina display","Landscape 12MP Ultra Wide camera","Apple Pencil Pro support","Touch ID","USB-C","All-day battery life"]', NOW(6), NOW(6));

-- 26: MacBook Air M3 13"
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(26, 'MacBook Air M3 13"', 3, NULL, 4.6, 3890, 'Popular',
'Strikingly thin. M3 chip. 13.6-inch Liquid Retina display. Great value.',
'MacBook Air M3 delivers exceptional performance and portability with the M3 chip. With a 13.6-inch Liquid Retina display, all-day battery life, and a fanless design, it is perfect for everyday tasks and creativity.',
'["M3 chip with 8-core GPU","13.6-inch Liquid Retina display","Up to 18 hours battery life","8GB unified memory","1080p FaceTime HD camera","MagSafe charging","Fanless design"]', NOW(6), NOW(6));

-- 27: MacBook Pro 14" M3
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(27, 'MacBook Pro 14" M3', 3, NULL, 4.7, 2156, NULL,
'M3 chip. 14-inch Liquid Retina XDR display. Pro performance at a great price.',
'MacBook Pro 14" M3 offers professional performance with the M3 chip, a stunning Liquid Retina XDR display, and up to 22 hours of battery life. Perfect for developers, designers, and content creators.',
'["M3 chip with 10-core GPU","14.2-inch Liquid Retina XDR display","Up to 22 hours battery life","8GB unified memory","Thunderbolt 4 ports","Studio-quality three-mic array","HDMI 2.0"]', NOW(6), NOW(6));

-- 28: Apple Watch SE 2
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(28, 'Apple Watch SE 2', 4, NULL, 4.4, 6789, 'Best Seller',
'S8 chip. Essential health features. Great value.',
'Apple Watch SE 2 brings essential health and safety features at an affordable price. With the S8 chip, fall detection, and activity tracking, it is the perfect entry point to Apple Watch.',
'["S8 chip","Fall detection and Crash Detection","Activity and sleep tracking","High and low heart rate notifications","40mm or 44mm case","Water resistant to 50m","Family Setup support"]', NOW(6), NOW(6));

-- 29: HomePod 2
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(29, 'HomePod 2', 5, NULL, 4.6, 2345, NULL,
'Room-filling sound. Smart assistant. The powerful smart speaker.',
'HomePod 2 delivers rich, room-filling sound with a high-excursion woofer and five beam-forming tweeters. With Siri, smart home controls, and multi-room audio, it is the heart of your home.',
'["Room-filling sound with high-excursion woofer","Five beam-forming tweeters","Siri with smart home controls","Multi-room audio with AirPlay","Temperature and humidity sensor","Sound recognition","Thread and Matter support"]', NOW(6), NOW(6));

-- 30: HomePod Mini
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(30, 'HomePod Mini', 5, NULL, 4.5, 4567, 'Best Seller',
'Big sound. Compact size. Smart assistant in every room.',
'HomePod Mini fills any room with surprisingly big sound. With Siri, intercom, and smart home controls, it is the perfect smart speaker for every room in your home.',
'["360-degree audio with full-range driver","Siri with smart home controls","Intercom for whole-home communication","Multi-room audio with AirPlay 2","Temperature and humidity sensor","Compact design","Thread support"]', NOW(6), NOW(6));

-- 31: Apple TV 4K
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(31, 'Apple TV 4K', 6, NULL, 4.7, 1890, NULL,
'A15 Bionic chip. HDR10+. Dolby Atmos. The ultimate streaming experience.',
'Apple TV 4K delivers the ultimate cinematic experience with the A15 Bionic chip, support for HDR10+ and Dolby Vision, and immersive Dolby Atmos audio. With the Siri Remote, you can control everything with your voice.',
'["A15 Bionic chip","4K HDR with Dolby Vision and HDR10+","Dolby Atmos","Siri Remote with USB-C","Wi-Fi 6 and Bluetooth 5.0","Apple Music Sing","Smart home controls"]', NOW(6), NOW(6));

-- 32: AirTag 4-Pack
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(32, 'AirTag 4-Pack', 6, NULL, 4.6, 12345, 'Best Seller',
'Keep track of your things. The four-pack for all your belongings.',
'AirTag is a super easy way to keep track of your stuff. Attach one to your keys, slip another in your backpack, and never worry about losing your things again. The Find My network helps you locate your items with precision tracking.',
'["Precision Finding with Ultra Wideband","Replaceable battery lasts over a year","Water resistant to IP67","Built-in speaker","Find My network with hundreds of millions of devices","Four AirTags included","Privacy-focused design"]', NOW(6), NOW(6));

-- 33: Apple Pencil Pro
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(33, 'Apple Pencil Pro', 6, NULL, 4.8, 3456, 'New',
'Squeeze, barrel roll, and haptic feedback. The ultimate creative tool.',
'Apple Pencil Pro delivers precision and versatility with new features like squeeze gesture, barrel roll for precise tool control, and haptic feedback. It brings your creative vision to life on iPad Pro and iPad Air.',
'["Squeeze gesture for quick tool switching","Barrel roll for precise tool control","Haptic feedback","Pixel-perfect precision and low latency","Hover support","Attaches magnetically","USB-C charging"]', NOW(6), NOW(6));

-- 34: Magic Keyboard for iPad Pro
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(34, 'Magic Keyboard for iPad Pro', 6, NULL, 4.7, 2341, NULL,
'Floating design. Trackpad. Backlit keys. The ultimate iPad Pro companion.',
'Magic Keyboard delivers an incredible typing experience with a floating cantilever design, built-in trackpad, and backlit keys. It transforms iPad Pro into a powerful laptop experience.',
'["Floating cantilever design","Built-in trackpad for precision","Backlit keys with scissor mechanism","USB-C pass-through charging","Smooth angle adjustment","Machine learning optimizations","Durable and portable"]', NOW(6), NOW(6));

-- 35: Apple 35W Dual USB-C Charger
INSERT INTO products (id, name, category_id, image, rating, reviews, badge, description, full_description, features, created_at, updated_at) VALUES
(35, 'Apple 35W Dual USB-C Charger', 6, NULL, 4.4, 5678, NULL,
'Charge two devices at once. Compact and powerful.',
'The Apple 35W Dual USB-C Charger lets you charge two devices simultaneously. Compact enough to take anywhere, it is perfect for iPhone, iPad, AirPods, and Apple Watch.',
'["35W total power","Two USB-C ports","Charge two devices simultaneously","Compact and portable design","Compatible with iPhone, iPad, AirPods, Apple Watch","USB-PD support"]', NOW(6), NOW(6));

-- ===================== VARIANTS =====================

-- iPhone 16 Pro Max (product_id=1)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(1, 1, '256GB', 1199.00, 50, 120, b'1', NOW(6), NOW(6)),
(1, 1, '512GB', 1399.00, 40, 85, b'1', NOW(6), NOW(6)),
(1, 1, '1TB', 1599.00, 30, 45, b'1', NOW(6), NOW(6)),
(1, 2, '256GB', 1199.00, 45, 95, b'1', NOW(6), NOW(6)),
(1, 2, '512GB', 1399.00, 35, 60, b'1', NOW(6), NOW(6)),
(1, 2, '1TB', 1599.00, 25, 30, b'1', NOW(6), NOW(6)),
(1, 3, '256GB', 1199.00, 40, 78, b'1', NOW(6), NOW(6)),
(1, 3, '512GB', 1399.00, 30, 55, b'1', NOW(6), NOW(6)),
(1, 3, '1TB', 1599.00, 20, 25, b'1', NOW(6), NOW(6)),
(1, 13, '256GB', 1199.00, 35, 67, b'1', NOW(6), NOW(6)),
(1, 13, '512GB', 1399.00, 25, 42, b'1', NOW(6), NOW(6)),
(1, 13, '1TB', 1599.00, 15, 18, b'1', NOW(6), NOW(6));

-- iPhone 16 Pro (product_id=2)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(2, 1, '128GB', 999.00, 60, 150, b'1', NOW(6), NOW(6)),
(2, 1, '256GB', 1099.00, 50, 110, b'1', NOW(6), NOW(6)),
(2, 1, '512GB', 1299.00, 40, 75, b'1', NOW(6), NOW(6)),
(2, 1, '1TB', 1499.00, 30, 35, b'1', NOW(6), NOW(6)),
(2, 2, '128GB', 999.00, 55, 130, b'1', NOW(6), NOW(6)),
(2, 2, '256GB', 1099.00, 45, 90, b'1', NOW(6), NOW(6)),
(2, 2, '512GB', 1299.00, 35, 60, b'1', NOW(6), NOW(6)),
(2, 2, '1TB', 1499.00, 25, 28, b'1', NOW(6), NOW(6)),
(2, 3, '128GB', 999.00, 50, 105, b'1', NOW(6), NOW(6)),
(2, 3, '256GB', 1099.00, 40, 80, b'1', NOW(6), NOW(6)),
(2, 3, '512GB', 1299.00, 30, 50, b'1', NOW(6), NOW(6)),
(2, 3, '1TB', 1499.00, 20, 20, b'1', NOW(6), NOW(6)),
(2, 13, '128GB', 999.00, 45, 90, b'1', NOW(6), NOW(6)),
(2, 13, '256GB', 1099.00, 35, 65, b'1', NOW(6), NOW(6)),
(2, 13, '512GB', 1299.00, 25, 40, b'1', NOW(6), NOW(6)),
(2, 13, '1TB', 1499.00, 15, 15, b'1', NOW(6), NOW(6));

-- iPhone 16 (product_id=3)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(3, 9, '128GB', 799.00, 70, 200, b'1', NOW(6), NOW(6)),
(3, 9, '256GB', 899.00, 60, 150, b'1', NOW(6), NOW(6)),
(3, 9, '512GB', 1099.00, 40, 60, b'1', NOW(6), NOW(6)),
(3, 7, '128GB', 799.00, 65, 180, b'1', NOW(6), NOW(6)),
(3, 7, '256GB', 899.00, 55, 130, b'1', NOW(6), NOW(6)),
(3, 7, '512GB', 1099.00, 35, 50, b'1', NOW(6), NOW(6)),
(3, 10, '128GB', 799.00, 60, 160, b'1', NOW(6), NOW(6)),
(3, 10, '256GB', 899.00, 50, 110, b'1', NOW(6), NOW(6)),
(3, 10, '512GB', 1099.00, 30, 40, b'1', NOW(6), NOW(6)),
(3, 5, '128GB', 799.00, 75, 220, b'1', NOW(6), NOW(6)),
(3, 5, '256GB', 899.00, 65, 160, b'1', NOW(6), NOW(6)),
(3, 5, '512GB', 1099.00, 45, 70, b'1', NOW(6), NOW(6)),
(3, 6, '128GB', 799.00, 70, 190, b'1', NOW(6), NOW(6)),
(3, 6, '256GB', 899.00, 60, 140, b'1', NOW(6), NOW(6)),
(3, 6, '512GB', 1099.00, 40, 55, b'1', NOW(6), NOW(6)),
(3, 11, '128GB', 799.00, 50, 130, b'1', NOW(6), NOW(6)),
(3, 11, '256GB', 899.00, 40, 90, b'1', NOW(6), NOW(6)),
(3, 11, '512GB', 1099.00, 25, 30, b'1', NOW(6), NOW(6));

-- iPhone 16 Plus (product_id=4)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(4, 9, '128GB', 899.00, 50, 90, b'1', NOW(6), NOW(6)),
(4, 9, '256GB', 999.00, 40, 60, b'1', NOW(6), NOW(6)),
(4, 9, '512GB', 1199.00, 25, 25, b'1', NOW(6), NOW(6)),
(4, 7, '128GB', 899.00, 45, 80, b'1', NOW(6), NOW(6)),
(4, 7, '256GB', 999.00, 35, 50, b'1', NOW(6), NOW(6)),
(4, 7, '512GB', 1199.00, 20, 20, b'1', NOW(6), NOW(6)),
(4, 10, '128GB', 899.00, 40, 70, b'1', NOW(6), NOW(6)),
(4, 10, '256GB', 999.00, 30, 45, b'1', NOW(6), NOW(6)),
(4, 10, '512GB', 1199.00, 15, 15, b'1', NOW(6), NOW(6)),
(4, 5, '128GB', 899.00, 55, 100, b'1', NOW(6), NOW(6)),
(4, 5, '256GB', 999.00, 45, 70, b'1', NOW(6), NOW(6)),
(4, 5, '512GB', 1199.00, 30, 30, b'1', NOW(6), NOW(6)),
(4, 6, '128GB', 899.00, 50, 85, b'1', NOW(6), NOW(6)),
(4, 6, '256GB', 999.00, 40, 55, b'1', NOW(6), NOW(6)),
(4, 6, '512GB', 1199.00, 25, 20, b'1', NOW(6), NOW(6)),
(4, 11, '128GB', 899.00, 35, 55, b'1', NOW(6), NOW(6)),
(4, 11, '256GB', 999.00, 25, 35, b'1', NOW(6), NOW(6)),
(4, 11, '512GB', 1199.00, 15, 10, b'1', NOW(6), NOW(6));

-- MacBook Pro 16 (product_id=5)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(5, 1, '512GB', 2499.00, 30, 120, b'1', NOW(6), NOW(6)),
(5, 1, '1TB', 2699.00, 25, 90, b'1', NOW(6), NOW(6)),
(5, 1, '2TB', 3099.00, 15, 45, b'1', NOW(6), NOW(6)),
(5, 2, '512GB', 2499.00, 25, 95, b'1', NOW(6), NOW(6)),
(5, 2, '1TB', 2699.00, 20, 70, b'1', NOW(6), NOW(6)),
(5, 2, '2TB', 3099.00, 10, 30, b'1', NOW(6), NOW(6));

-- MacBook Pro 14 (product_id=6)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(6, 1, '512GB', 1999.00, 35, 145, b'1', NOW(6), NOW(6)),
(6, 1, '1TB', 2199.00, 30, 100, b'1', NOW(6), NOW(6)),
(6, 1, '2TB', 2599.00, 20, 50, b'1', NOW(6), NOW(6)),
(6, 2, '512GB', 1999.00, 30, 115, b'1', NOW(6), NOW(6)),
(6, 2, '1TB', 2199.00, 25, 80, b'1', NOW(6), NOW(6)),
(6, 2, '2TB', 2599.00, 15, 35, b'1', NOW(6), NOW(6));

-- MacBook Air M4 (product_id=7)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(7, 5, '256GB', 1099.00, 40, 100, b'1', NOW(6), NOW(6)),
(7, 5, '512GB', 1299.00, 35, 75, b'1', NOW(6), NOW(6)),
(7, 5, '1TB', 1499.00, 25, 40, b'1', NOW(6), NOW(6)),
(7, 6, '256GB', 1099.00, 35, 85, b'1', NOW(6), NOW(6)),
(7, 6, '512GB', 1299.00, 30, 65, b'1', NOW(6), NOW(6)),
(7, 6, '1TB', 1499.00, 20, 30, b'1', NOW(6), NOW(6)),
(7, 2, '256GB', 1099.00, 30, 70, b'1', NOW(6), NOW(6)),
(7, 2, '512GB', 1299.00, 25, 55, b'1', NOW(6), NOW(6)),
(7, 2, '1TB', 1499.00, 15, 25, b'1', NOW(6), NOW(6)),
(7, 4, '256GB', 1099.00, 35, 90, b'1', NOW(6), NOW(6)),
(7, 4, '512GB', 1299.00, 30, 70, b'1', NOW(6), NOW(6)),
(7, 4, '1TB', 1499.00, 20, 35, b'1', NOW(6), NOW(6));

-- iMac 24 (product_id=8)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(8, 7, '256GB', 1299.00, 20, 60, b'1', NOW(6), NOW(6)),
(8, 7, '512GB', 1499.00, 15, 40, b'1', NOW(6), NOW(6)),
(8, 7, '1TB', 1699.00, 10, 20, b'1', NOW(6), NOW(6)),
(8, 10, '256GB', 1299.00, 20, 45, b'1', NOW(6), NOW(6)),
(8, 10, '512GB', 1499.00, 15, 30, b'1', NOW(6), NOW(6)),
(8, 10, '1TB', 1699.00, 10, 15, b'1', NOW(6), NOW(6)),
(8, 9, '256GB', 1299.00, 20, 55, b'1', NOW(6), NOW(6)),
(8, 9, '512GB', 1499.00, 15, 35, b'1', NOW(6), NOW(6)),
(8, 9, '1TB', 1699.00, 10, 18, b'1', NOW(6), NOW(6)),
(8, 2, '256GB', 1299.00, 25, 70, b'1', NOW(6), NOW(6)),
(8, 2, '512GB', 1499.00, 20, 45, b'1', NOW(6), NOW(6)),
(8, 2, '1TB', 1699.00, 15, 22, b'1', NOW(6), NOW(6)),
(8, 14, '256GB', 1299.00, 15, 35, b'1', NOW(6), NOW(6)),
(8, 14, '512GB', 1499.00, 10, 20, b'1', NOW(6), NOW(6)),
(8, 14, '1TB', 1699.00, 5, 8, b'1', NOW(6), NOW(6)),
(8, 15, '256GB', 1299.00, 15, 30, b'1', NOW(6), NOW(6)),
(8, 15, '512GB', 1499.00, 10, 18, b'1', NOW(6), NOW(6)),
(8, 15, '1TB', 1699.00, 5, 5, b'1', NOW(6), NOW(6)),
(8, 8, '256GB', 1299.00, 20, 40, b'1', NOW(6), NOW(6)),
(8, 8, '512GB', 1499.00, 15, 25, b'1', NOW(6), NOW(6)),
(8, 8, '1TB', 1699.00, 10, 12, b'1', NOW(6), NOW(6));

-- iPad Pro M4 13 (product_id=9)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(9, 1, '256GB', 1299.00, 25, 60, b'1', NOW(6), NOW(6)),
(9, 1, '512GB', 1499.00, 20, 45, b'1', NOW(6), NOW(6)),
(9, 1, '1TB', 1899.00, 15, 30, b'1', NOW(6), NOW(6)),
(9, 1, '2TB', 2299.00, 10, 12, b'1', NOW(6), NOW(6)),
(9, 2, '256GB', 1299.00, 20, 50, b'1', NOW(6), NOW(6)),
(9, 2, '512GB', 1499.00, 15, 35, b'1', NOW(6), NOW(6)),
(9, 2, '1TB', 1899.00, 10, 20, b'1', NOW(6), NOW(6)),
(9, 2, '2TB', 2299.00, 5, 8, b'1', NOW(6), NOW(6));

-- iPad Pro M4 11 (product_id=10)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(10, 1, '256GB', 999.00, 30, 80, b'1', NOW(6), NOW(6)),
(10, 1, '512GB', 1199.00, 25, 55, b'1', NOW(6), NOW(6)),
(10, 1, '1TB', 1599.00, 15, 25, b'1', NOW(6), NOW(6)),
(10, 1, '2TB', 1999.00, 10, 10, b'1', NOW(6), NOW(6)),
(10, 2, '256GB', 999.00, 25, 65, b'1', NOW(6), NOW(6)),
(10, 2, '512GB', 1199.00, 20, 40, b'1', NOW(6), NOW(6)),
(10, 2, '1TB', 1599.00, 10, 18, b'1', NOW(6), NOW(6)),
(10, 2, '2TB', 1999.00, 5, 5, b'1', NOW(6), NOW(6));

-- iPad Air M2 13 (product_id=11)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(11, 7, '128GB', 599.00, 40, 120, b'1', NOW(6), NOW(6)),
(11, 7, '256GB', 699.00, 35, 90, b'1', NOW(6), NOW(6)),
(11, 7, '512GB', 899.00, 25, 50, b'1', NOW(6), NOW(6)),
(11, 7, '1TB', 1099.00, 15, 20, b'1', NOW(6), NOW(6)),
(11, 8, '128GB', 599.00, 35, 100, b'1', NOW(6), NOW(6)),
(11, 8, '256GB', 699.00, 30, 75, b'1', NOW(6), NOW(6)),
(11, 8, '512GB', 899.00, 20, 40, b'1', NOW(6), NOW(6)),
(11, 8, '1TB', 1099.00, 10, 15, b'1', NOW(6), NOW(6)),
(11, 6, '128GB', 599.00, 45, 130, b'1', NOW(6), NOW(6)),
(11, 6, '256GB', 699.00, 40, 95, b'1', NOW(6), NOW(6)),
(11, 6, '512GB', 899.00, 28, 55, b'1', NOW(6), NOW(6)),
(11, 6, '1TB', 1099.00, 18, 22, b'1', NOW(6), NOW(6)),
(11, 4, '128GB', 599.00, 38, 110, b'1', NOW(6), NOW(6)),
(11, 4, '256GB', 699.00, 32, 80, b'1', NOW(6), NOW(6)),
(11, 4, '512GB', 899.00, 22, 45, b'1', NOW(6), NOW(6)),
(11, 4, '1TB', 1099.00, 12, 18, b'1', NOW(6), NOW(6));

-- iPad 10th Gen (product_id=12)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(12, 7, '64GB', 449.00, 50, 180, b'1', NOW(6), NOW(6)),
(12, 7, '256GB', 599.00, 40, 100, b'1', NOW(6), NOW(6)),
(12, 9, '64GB', 449.00, 45, 150, b'1', NOW(6), NOW(6)),
(12, 9, '256GB', 599.00, 35, 85, b'1', NOW(6), NOW(6)),
(12, 2, '64GB', 449.00, 55, 200, b'1', NOW(6), NOW(6)),
(12, 2, '256GB', 599.00, 45, 110, b'1', NOW(6), NOW(6)),
(12, 14, '64GB', 449.00, 40, 130, b'1', NOW(6), NOW(6)),
(12, 14, '256GB', 599.00, 30, 70, b'1', NOW(6), NOW(6));

-- Apple Watch Ultra 3 (product_id=13)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(13, 12, '32GB', 799.00, 25, 80, b'1', NOW(6), NOW(6)),
(13, 12, '64GB', 849.00, 20, 45, b'1', NOW(6), NOW(6)),
(13, 1, '32GB', 799.00, 20, 55, b'1', NOW(6), NOW(6)),
(13, 1, '64GB', 849.00, 15, 30, b'1', NOW(6), NOW(6));

-- Apple Watch Series 10 (product_id=14)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(14, 5, '41mm', 399.00, 40, 150, b'1', NOW(6), NOW(6)),
(14, 5, '45mm', 429.00, 35, 100, b'1', NOW(6), NOW(6)),
(14, 6, '41mm', 399.00, 35, 120, b'1', NOW(6), NOW(6)),
(14, 6, '45mm', 429.00, 30, 80, b'1', NOW(6), NOW(6)),
(14, 2, '41mm', 399.00, 30, 90, b'1', NOW(6), NOW(6)),
(14, 2, '45mm', 429.00, 25, 60, b'1', NOW(6), NOW(6)),
(14, 11, '41mm', 399.00, 25, 70, b'1', NOW(6), NOW(6)),
(14, 11, '45mm', 429.00, 20, 45, b'1', NOW(6), NOW(6));

-- AirPods Pro 3 (product_id=15)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(15, 12, NULL, 249.00, 100, 500, b'1', NOW(6), NOW(6));

-- AirPods Max 2 (product_id=16)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(16, 2, NULL, 549.00, 30, 80, b'1', NOW(6), NOW(6)),
(16, 4, NULL, 549.00, 25, 65, b'1', NOW(6), NOW(6)),
(16, 7, NULL, 549.00, 20, 45, b'1', NOW(6), NOW(6)),
(16, 9, NULL, 549.00, 20, 50, b'1', NOW(6), NOW(6)),
(16, 10, NULL, 549.00, 15, 30, b'1', NOW(6), NOW(6));

-- AirPods 4 (product_id=17)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(17, 12, NULL, 129.00, 150, 800, b'1', NOW(6), NOW(6));

-- Mac Mini M4 (product_id=18)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(18, 2, '256GB', 599.00, 25, 60, b'1', NOW(6), NOW(6)),
(18, 2, '512GB', 799.00, 20, 40, b'1', NOW(6), NOW(6)),
(18, 2, '1TB', 1099.00, 15, 20, b'1', NOW(6), NOW(6));

-- Mac Studio M4 Max (product_id=19)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(19, 2, '512GB', 1999.00, 15, 35, b'1', NOW(6), NOW(6)),
(19, 2, '1TB', 2199.00, 12, 25, b'1', NOW(6), NOW(6)),
(19, 2, '2TB', 2599.00, 8, 12, b'1', NOW(6), NOW(6));

-- Mac Pro (product_id=20)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(20, 2, '1TB', 6999.00, 5, 15, b'1', NOW(6), NOW(6)),
(20, 2, '2TB', 7499.00, 3, 8, b'1', NOW(6), NOW(6)),
(20, 2, '4TB', 8499.00, 2, 3, b'1', NOW(6), NOW(6));

-- iPhone 15 Pro Max (product_id=21)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(21, 1, '256GB', 1099.00, 35, 320, b'1', NOW(6), NOW(6)),
(21, 1, '512GB', 1299.00, 25, 180, b'1', NOW(6), NOW(6)),
(21, 1, '1TB', 1499.00, 15, 70, b'1', NOW(6), NOW(6)),
(21, 2, '256GB', 1099.00, 30, 280, b'1', NOW(6), NOW(6)),
(21, 2, '512GB', 1299.00, 20, 150, b'1', NOW(6), NOW(6)),
(21, 2, '1TB', 1499.00, 10, 55, b'1', NOW(6), NOW(6)),
(21, 3, '256GB', 1099.00, 25, 200, b'1', NOW(6), NOW(6)),
(21, 3, '512GB', 1299.00, 15, 110, b'1', NOW(6), NOW(6)),
(21, 3, '1TB', 1499.00, 8, 35, b'1', NOW(6), NOW(6)),
(21, 13, '256GB', 1099.00, 20, 160, b'1', NOW(6), NOW(6)),
(21, 13, '512GB', 1299.00, 12, 90, b'1', NOW(6), NOW(6)),
(21, 13, '1TB', 1499.00, 5, 25, b'1', NOW(6), NOW(6));

-- iPhone 15 (product_id=22)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(22, 9, '128GB', 699.00, 65, 480, b'1', NOW(6), NOW(6)),
(22, 9, '256GB', 799.00, 55, 320, b'1', NOW(6), NOW(6)),
(22, 9, '512GB', 999.00, 35, 120, b'1', NOW(6), NOW(6)),
(22, 7, '128GB', 699.00, 60, 420, b'1', NOW(6), NOW(6)),
(22, 7, '256GB', 799.00, 50, 280, b'1', NOW(6), NOW(6)),
(22, 7, '512GB', 999.00, 30, 100, b'1', NOW(6), NOW(6)),
(22, 10, '128GB', 699.00, 55, 380, b'1', NOW(6), NOW(6)),
(22, 10, '256GB', 799.00, 45, 250, b'1', NOW(6), NOW(6)),
(22, 10, '512GB', 999.00, 25, 85, b'1', NOW(6), NOW(6)),
(22, 5, '128GB', 699.00, 70, 550, b'1', NOW(6), NOW(6)),
(22, 5, '256GB', 799.00, 60, 360, b'1', NOW(6), NOW(6)),
(22, 5, '512GB', 999.00, 40, 140, b'1', NOW(6), NOW(6)),
(22, 6, '128GB', 699.00, 65, 450, b'1', NOW(6), NOW(6)),
(22, 6, '256GB', 799.00, 55, 300, b'1', NOW(6), NOW(6)),
(22, 6, '512GB', 999.00, 35, 110, b'1', NOW(6), NOW(6));

-- iPhone SE 3 (product_id=23)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(23, 5, '64GB', 429.00, 80, 600, b'1', NOW(6), NOW(6)),
(23, 5, '128GB', 479.00, 70, 450, b'1', NOW(6), NOW(6)),
(23, 5, '256GB', 579.00, 50, 200, b'1', NOW(6), NOW(6)),
(23, 6, '64GB', 429.00, 75, 520, b'1', NOW(6), NOW(6)),
(23, 6, '128GB', 479.00, 65, 380, b'1', NOW(6), NOW(6)),
(23, 6, '256GB', 579.00, 45, 160, b'1', NOW(6), NOW(6)),
(23, 11, '64GB', 429.00, 60, 380, b'1', NOW(6), NOW(6)),
(23, 11, '128GB', 479.00, 50, 260, b'1', NOW(6), NOW(6)),
(23, 11, '256GB', 579.00, 30, 100, b'1', NOW(6), NOW(6));

-- iPad Mini 7 (product_id=24)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(24, 7, '128GB', 499.00, 40, 120, b'1', NOW(6), NOW(6)),
(24, 7, '256GB', 599.00, 30, 70, b'1', NOW(6), NOW(6)),
(24, 7, '512GB', 799.00, 20, 30, b'1', NOW(6), NOW(6)),
(24, 8, '128GB', 499.00, 35, 100, b'1', NOW(6), NOW(6)),
(24, 8, '256GB', 599.00, 25, 55, b'1', NOW(6), NOW(6)),
(24, 8, '512GB', 799.00, 15, 20, b'1', NOW(6), NOW(6)),
(24, 6, '128GB', 499.00, 45, 140, b'1', NOW(6), NOW(6)),
(24, 6, '256GB', 599.00, 35, 85, b'1', NOW(6), NOW(6)),
(24, 6, '512GB', 799.00, 25, 40, b'1', NOW(6), NOW(6)),
(24, 4, '128GB', 499.00, 38, 110, b'1', NOW(6), NOW(6)),
(24, 4, '256GB', 599.00, 28, 65, b'1', NOW(6), NOW(6)),
(24, 4, '512GB', 799.00, 18, 25, b'1', NOW(6), NOW(6));

-- iPad Air M2 11" (product_id=25)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(25, 7, '128GB', 549.00, 45, 160, b'1', NOW(6), NOW(6)),
(25, 7, '256GB', 649.00, 35, 100, b'1', NOW(6), NOW(6)),
(25, 7, '512GB', 849.00, 25, 55, b'1', NOW(6), NOW(6)),
(25, 7, '1TB', 1049.00, 15, 20, b'1', NOW(6), NOW(6)),
(25, 8, '128GB', 549.00, 40, 130, b'1', NOW(6), NOW(6)),
(25, 8, '256GB', 649.00, 30, 80, b'1', NOW(6), NOW(6)),
(25, 8, '512GB', 849.00, 20, 40, b'1', NOW(6), NOW(6)),
(25, 8, '1TB', 1049.00, 10, 15, b'1', NOW(6), NOW(6)),
(25, 6, '128GB', 549.00, 50, 180, b'1', NOW(6), NOW(6)),
(25, 6, '256GB', 649.00, 40, 110, b'1', NOW(6), NOW(6)),
(25, 6, '512GB', 849.00, 28, 60, b'1', NOW(6), NOW(6)),
(25, 6, '1TB', 1049.00, 18, 22, b'1', NOW(6), NOW(6)),
(25, 4, '128GB', 549.00, 42, 140, b'1', NOW(6), NOW(6)),
(25, 4, '256GB', 649.00, 32, 90, b'1', NOW(6), NOW(6)),
(25, 4, '512GB', 849.00, 22, 50, b'1', NOW(6), NOW(6)),
(25, 4, '1TB', 1049.00, 12, 18, b'1', NOW(6), NOW(6));

-- MacBook Air M3 13" (product_id=26)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(26, 5, '256GB', 999.00, 45, 280, b'1', NOW(6), NOW(6)),
(26, 5, '512GB', 1199.00, 35, 180, b'1', NOW(6), NOW(6)),
(26, 5, '1TB', 1399.00, 25, 80, b'1', NOW(6), NOW(6)),
(26, 6, '256GB', 999.00, 40, 240, b'1', NOW(6), NOW(6)),
(26, 6, '512GB', 1199.00, 30, 150, b'1', NOW(6), NOW(6)),
(26, 6, '1TB', 1399.00, 20, 60, b'1', NOW(6), NOW(6)),
(26, 2, '256GB', 999.00, 35, 200, b'1', NOW(6), NOW(6)),
(26, 2, '512GB', 1199.00, 25, 120, b'1', NOW(6), NOW(6)),
(26, 2, '1TB', 1399.00, 15, 45, b'1', NOW(6), NOW(6)),
(26, 4, '256GB', 999.00, 40, 220, b'1', NOW(6), NOW(6)),
(26, 4, '512GB', 1199.00, 30, 130, b'1', NOW(6), NOW(6)),
(26, 4, '1TB', 1399.00, 20, 50, b'1', NOW(6), NOW(6));

-- MacBook Pro 14" M3 (product_id=27)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(27, 1, '512GB', 1599.00, 30, 140, b'1', NOW(6), NOW(6)),
(27, 1, '1TB', 1799.00, 25, 90, b'1', NOW(6), NOW(6)),
(27, 2, '512GB', 1599.00, 25, 110, b'1', NOW(6), NOW(6)),
(27, 2, '1TB', 1799.00, 20, 70, b'1', NOW(6), NOW(6));

-- Apple Watch SE 2 (product_id=28)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(28, 5, '40mm', 249.00, 60, 450, b'1', NOW(6), NOW(6)),
(28, 5, '44mm', 279.00, 50, 320, b'1', NOW(6), NOW(6)),
(28, 6, '40mm', 249.00, 55, 380, b'1', NOW(6), NOW(6)),
(28, 6, '44mm', 279.00, 45, 260, b'1', NOW(6), NOW(6)),
(28, 2, '40mm', 249.00, 50, 350, b'1', NOW(6), NOW(6)),
(28, 2, '44mm', 279.00, 40, 240, b'1', NOW(6), NOW(6));

-- HomePod 2 (product_id=29)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(29, 12, NULL, 299.00, 40, 200, b'1', NOW(6), NOW(6)),
(29, 5, NULL, 299.00, 35, 150, b'1', NOW(6), NOW(6));

-- HomePod Mini (product_id=30)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(30, 12, NULL, 99.00, 80, 800, b'1', NOW(6), NOW(6)),
(30, 7, NULL, 99.00, 50, 300, b'1', NOW(6), NOW(6)),
(30, 15, NULL, 99.00, 40, 200, b'1', NOW(6), NOW(6)),
(30, 14, NULL, 99.00, 35, 180, b'1', NOW(6), NOW(6));

-- Apple TV 4K (product_id=31)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(31, 1, '64GB', 129.00, 45, 300, b'1', NOW(6), NOW(6)),
(31, 1, '128GB', 149.00, 35, 180, b'1', NOW(6), NOW(6));

-- AirTag 4-Pack (product_id=32)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(32, 12, NULL, 99.00, 60, 1200, b'1', NOW(6), NOW(6));

-- Apple Pencil Pro (product_id=33)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(33, 12, NULL, 129.00, 70, 800, b'1', NOW(6), NOW(6));

-- Magic Keyboard for iPad Pro (product_id=34)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(34, 12, NULL, 349.00, 30, 150, b'1', NOW(6), NOW(6)),
(34, 1, NULL, 349.00, 25, 100, b'1', NOW(6), NOW(6));

-- Apple 35W Dual USB-C Charger (product_id=35)
INSERT INTO product_variants (product_id, color_id, storage, price, stock, sold, is_in_stock, created_at, updated_at) VALUES
(35, 12, NULL, 39.00, 100, 2000, b'1', NOW(6), NOW(6));

-- Update product_count in categories
UPDATE categories SET product_count = 7 WHERE slug = 'iphone';
UPDATE categories SET product_count = 6 WHERE slug = 'ipad';
UPDATE categories SET product_count = 8 WHERE slug = 'mac';
UPDATE categories SET product_count = 3 WHERE slug = 'watch';
UPDATE categories SET product_count = 5 WHERE slug = 'airpods-audio';
UPDATE categories SET product_count = 5 WHERE slug = 'accessories';
