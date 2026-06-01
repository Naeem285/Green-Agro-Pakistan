package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentLime
import com.example.ui.theme.PrimaryGreen

data class TimelinePhase(
    val emoji: String,
    val name: String,
    val dates: String,
    val note: String
)

data class CropData(
    val id: String,
    val name: String,
    val emoji: String,
    val season: String, // Rabi, Kharif, Year-round
    val tagline: String,
    val imageUrl: String,
    val description: String,
    val phases: List<TimelinePhase>,
    val tips: List<String>,
    val gallery: List<String>
)

object CropRepository {
    val crops = listOf(
        CropData(
            id = "wheat",
            name = "Wheat (گندم)",
            emoji = "🌾",
            season = "Rabi Season",
            tagline = "Pakistan's primary staple crop sown in winter and harvested in spring.",
            imageUrl = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600&auto=format&fit=crop&q=80",
            description = "Wheat is the most vital agricultural crop in Pakistan, serving as the primary staple food source of the population. Sown primarily during late autumn (Oct-Nov) across Punjab and Sindh, it matures under dry cold conditions and is harvested in peak spring (Apr-May).\n\nOptimal field hydration schedules and accurate balanced fertilization (DAP combined with timely Nitrogen top-ups) directly impacts per-acre grain yields by up to 30%. Sowing should ideally utilize certified disease-resistant varieties developed by premier institutes.",
            phases = listOf(
                TimelinePhase("🌱", "Sowing & Bed", "15 Oct - 15 Nov", "Sow AARI-2022 seeds with 40-45kg/acre density. Incorporate 1.5 bags of DAP."),
                TimelinePhase("💧", "First Irrigation", "3 weeks layer", "Provide first watering 20-22 days from sprouting (crown root development phase)."),
                TimelinePhase("🌾", "Booting / Milk", "Feb - Mar", "Provide crucial hydration during spike emergence. Control aphids using neem spray."),
                TimelinePhase("🚜", "Harvest Output", "10 Apr - 10 May", "Combine harvest on a dry sunny afternoon (yield expectancy: 45-55 Mons/acre).")
            ),
            tips = listOf(
                "Incorporate certified seeds for +20% spikelet density.",
                "Avoid heavy irrigation during strong winds to prevent lodging.",
                "Ensure timely eradication of rust pathogens early in Feb.",
                "Apply zinc sulphate to sandy soils during seedbed placement."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1542614131-2d4e7f83ad74?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1444653300346-3855907947d3?w=400&auto=format&fit=crop"
            )
        ),
        CropData(
            id = "rice",
            name = "Basmati Rice (چاول)",
            emoji = "🍚",
            season = "Kharif Season",
            tagline = "Premium aromatic export crop growing in waterlogged summer paddies.",
            imageUrl = "https://images.unsplash.com/photo-1536304997881-a372c179924b?w=600&auto=format&fit=crop&q=80",
            description = "Premium Pakistani Basmati Rice is world-renowned for its distinct aroma and elongated long grains. Sown in wet monsoon paddies, it requires heavy organic rich soil and consistent water standing throughout vegetative growth.\n\nControlling stem borers and maintaining early organic biological control are essential parameters to guarantee flawless exports. Sowing typically begins in nursery beds during May, followed by grueling manual field transplanting under scorching July monsoons.",
            phases = listOf(
                TimelinePhase("🌱", "Nursery Bedding", "1 May - 31 May", "Prepare moist nursery beds using 10kg seed/acre. Shield from birds."),
                TimelinePhase("🌾", "Paddy Transplant", "15 Jun - 10 Jul", "Transplant 2-3 seedlings per cluster with strict 20x20cm row spacing."),
                TimelinePhase("💧", "Fertilizer Boost", "30 days later", "Broadcast 2 bags of Urea split-dosed plus 1 bag of potash booster."),
                TimelinePhase("🚜", "Mandi Harvest", "1 Oct - 31 Oct", "Drain fields 2 weeks prior to harvest. Cut basmati when grains golden (35-45 Mons).")
            ),
            tips = listOf(
                "Utilize certified PK-386 or Super Basmati lines.",
                "Use Furadan granular treatment against paddy stem borer.",
                "A continuous water sheet is critical for the initial 40 days.",
                "Check moisture levels: market prefers 14% maximum humidity."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1599819811279-d5ad9cccf838?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1516253593875-bd7ba052fbc5?w=400&auto=format&fit=crop"
            )
        ),
        CropData(
            id = "cotton",
            name = "Cotton (کپاس)",
            emoji = "🌸",
            season = "Kharif Season",
            tagline = "The silver fiber of Pakistan, boosting textile spinning mills.",
            imageUrl = "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?w=600&auto=format&fit=crop&q=80",
            description = "Cotton represents the primary engine of Pakistan's vital textile industrial sector. It thrives under high heat sunshine with deep silt sediment soils, typically in South Punjab and Upper Sindh.\n\nWhitefly control remains the toughest challenge for growers. Sowing certified BT Cotton hybrids provides protection from bollworms, but precise IPM (Integrated Pest Management) schedules must be followed to prevent crop failures.",
            phases = listOf(
                TimelinePhase("🌱", "Drill Sowing", "15 Apr - 31 May", "Sow BT Cotton seed lines at 1ft seed-to-seed, 2.5ft row-to-row spacing."),
                TimelinePhase("✂️", "Thinning", "30 days later", "Eradicate weak growth elements to promote primary stalk ventilation."),
                TimelinePhase("🐛", "Pest Management", "Jul - Oct", "Conduct daily pest scouts. Deploy dimethoate if thrips exceed threshold."),
                TimelinePhase("🚜", "Manual Picking", "Oct - Dec", "Manually harvest matured fluffy white bolls over multiple pick passes.")
            ),
            tips = listOf(
                "Deploy sticky yellow traps for natural whitefly trapping.",
                "Avoid excess Nitrogen which attracts sap-sucking thrips.",
                "Adopt certified BT lines: IUB-222 or NIBGE-115.",
                "Commence picking only after morning dew completely dries."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1594489428504-5c0c480a15fd?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1516253593875-bd7ba052fbc5?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400&auto=format&fit=crop"
            )
        ),
        CropData(
            id = "sugarcane",
            name = "Sugarcane (گنا)",
            emoji = "🎋",
            season = "Year-round",
            tagline = "Long-cycle cash crop driving Pakistan's massive sweetener production.",
            imageUrl = "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=600&auto=format&fit=crop&q=80",
            description = "Sugarcane is an intensive perennial water cash crop critical for the sweetener supply chain of Pakistan. Growing over a full 10-12 month cycle, it is a key commodity for sugarcane crushing mills.\n\nIt requires deep clay loam soil and generous water irrigation. Successive ratoon crops are common in Punjab fields, where roots sprout again on the second year, reducing seedbed laying costs.",
            phases = listOf(
                TimelinePhase("🌱", "Sett Placement", "Feb - Mar", "Lay 3-eye seed cane setts inside deep 3-foot wide furrows."),
                TimelinePhase("🚜", "Earthing Up", "45 - 60 days", "Pack soil around cane bases to foster root anchoring and prevent lodging."),
                TimelinePhase("💧", "Hydration Cycle", "Bi-weekly", "Irrigate aggressively during hot summers to avoid cane dry hollows."),
                TimelinePhase("🎋", "Mill Crushing", "Nov - Jan", "Cut cane at base level when bottom leaves dry. Load mill carts.")
            ),
            tips = listOf(
                "Apply Atrazine early to control wide-leaf weeds.",
                "Excellent ratoon crops possible for 2-3 successive seasons.",
                "Avoid field waterlogging: layout exit channels for excess monsoon.",
                "Incorporate organic filter cake compost for high brix sugar contents."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1444653300346-3855907947d3?w=400&auto=format&fit=crop"
            )
        ),
        CropData(
            id = "maize",
            name = "Maize (مکئی)",
            emoji = "🌽",
            season = "Kharif & Spring",
            tagline = "High-demand fodder and starch grain with rapid turnaround cycle.",
            imageUrl = "https://images.unsplash.com/photo-1551754622-9eff20fe98d4?w=600&auto=format&fit=crop&q=80",
            description = "Maize is rapidly expanding across Pakistan, fueled by high poultry feed demand and industrial starch starch processors. Sown as both Spring and Autumn varieties, it is a short-cycle high-yielding feed crop.\n\nIrrigation control and armyworm defense are critical milestones. Incorporating hybrid seeds like Pioneer 3025 with precise drip lines maximizes crop returns significantly.",
            phases = listOf(
                TimelinePhase("🌱", "Ridge Laying", "Apr - May", "Sow hybrid seeds along ridges (8kg/acre). Maintain damp beds."),
                TimelinePhase("✂️", "Thinning Pass", "20 days later", "Ensure singular healthy shoots stand. Keep rows ventilated."),
                TimelinePhase("🐛", "Armyworm Scout", "30 days", "Perform leaf pest scout checks. Spray Spinetoram immediately if spotted."),
                TimelinePhase("🌽", "Dry Harvesting", "Aug - Sep", "Harvest cob when husk papery dry and kernels display central dent.")
            ),
            tips = listOf(
                "Verify high hybrid lines: Pioneer 3025 or DK-6789.",
                "Drip irrigation saves 40% water while adding +30% yield.",
                "Urea top-ups are vital during tasseling stages.",
                "Ensure quick thresher processing to prevent aflatoxin mold."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1551754622-9eff20fe98d4?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400&auto=format&fit=crop"
            )
        ),
        CropData(
            id = "vegetables",
            name = "Vegetables (سبزیاں)",
            emoji = "🥬",
            season = "Year-round",
            tagline = "Short-rotation cash crops for local farmer markets.",
            imageUrl = "https://images.unsplash.com/photo-1566385278631-7b2467214244?w=600&auto=format&fit=crop&q=80",
            description = "Short-rotation high-margin vegetables (Potatoes, Tomatoes, Chillies, Onions) provide daily cash flow to Pakistani smallholders. They are cultivated on raised beds with mulch sheets and high-care drip tubes for fast returns.\n\nSuccess relies on highly localized seed varieties and strict soil sanitation against fungal wilt and nematode pathogens.",
            phases = listOf(
                TimelinePhase("🌱", "Bed Preparation", "Sowing Season", "Form high uniform beds of sandy loam. Add organic manure base."),
                TimelinePhase("🌱", "Seedling Bed", "Varies", "Raise fragile tomato/chilli seedlings in greenhouse coco peat trays."),
                TimelinePhase("💧", "Hydration Routine", "Daily / Damp", "Avoid flooding. Use modern surface drip pipes twice a day."),
                TimelinePhase("🥬", "Graded Harvest", "Gradual", "Harvest in early crisp mornings. Grade sizes nicely before boxing.")
            ),
            tips = listOf(
                "Always utilize silver-black plastic mulch sheets.",
                "Sprinkle neem water repellent against aphid leaf rolls.",
                "Prune lower suckers off tomato vines to maximize sizing.",
                "Apply potash sprays for rich flavor and thick skin grades."
            ),
            gallery = listOf(
                "https://images.unsplash.com/photo-1566385278631-7b2467214244?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=400&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1516253593875-bd7ba052fbc5?w=400&auto=format&fit=crop"
            )
        )
    )
}

@Composable
fun CropsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCropId by remember { mutableStateOf<String?>(null) }

    if (selectedCropId != null) {
        val crop = CropRepository.crops.find { it.id == selectedCropId }
        if (crop != null) {
            CropDetailScreen(crop = crop, onBack = { selectedCropId = null })
        } else {
            selectedCropId = null
        }
    } else {
        CropCalendarList(onSelectCrop = { selectedCropId = it }, modifier = modifier)
    }
}

@Composable
fun CropCalendarList(
    onSelectCrop: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(title = "Pakistan Crop Calendar (2026 Season Advisor)")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(CropRepository.crops) { crop ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCrop(crop.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = crop.emoji, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = crop.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryGreen.copy(alpha = 0.08f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = crop.season,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = crop.tagline,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailScreen(
    crop: CropData,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(crop.name, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Unsplash Cached image banner
            AsyncImage(
                model = crop.imageUrl,
                contentDescription = crop.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Header elements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AGRICULTURE BLUEPRINT",
                        color = PrimaryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentLime)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = crop.season,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = crop.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = crop.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Growth Phases timeline
                Text(
                    text = "📈 Growth Milestones & Cycles",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                crop.phases.forEach { phase ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = phase.emoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = phase.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = phase.dates, color = PrimaryGreen, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }
                                Text(text = phase.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pro Tips bullets list
                Text(
                    text = "💡 Practical Agronomist Advice",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                crop.tips.forEach { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "plant tip",
                            tint = AccentLime,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tip,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scroll Image gallery
                Text(
                    text = "🖼️ Crop Visual Gallery",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(crop.gallery) { picUrl ->
                        AsyncImage(
                            model = picUrl,
                            contentDescription = "Crop gallery image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 120.dp, height = 90.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
