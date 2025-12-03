package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.local.ChecklistDao
import ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem

data class ChecklistLink(val label: String, val url: String, val category: String)

private val CHECK_ITEMS: List<ChecklistLink> = listOf(
    // Water & Food
    ChecklistLink(
        "Water Purification Tablets / Aquatabs",
        "https://shopee.ph/product/727871302/16440875121?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrdkhxNVhDNGN6OUJaN0U1NHpxaGNGQzRyTGtYd3FsVFBMbDJRdmxOZytOWXhkb2U0T0NJaitQRzFqdUpycXdEak55WWZzT1M5UFBnSUlQUVU2NmxxUmw&gad_source=1&gad_campaignid=1674903539&gbraid=0AAAAADPpU9DjcaHcNFxS6Tn7vBE6E_KRf&gclid=CjwKCAiA3L_JBhAlEiwAlcWO58Aft92ySpURDTSefPy96r4NmGr07p4o_ZKIWEd1ziWPbzNeYprqOhoC6sQQAvD_BwE",
        "Water & Food"
    ),
    ChecklistLink(
        "Collapsible Water Container (Jerrican)",
        "https://shopee.ph/product/308002767/17954535383?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrdGlpMzRPUkZSWVpGZHhLdWVCSDBxQ056dFBZZUxFc2RiVmZ0VXh4OWQrM0cyR3Rnc3VYSWZkNVBUZXMzNWc2bVpiSFdXa3k5NHVwYzV0M1lwMHdmYmY&gad_source=1&gad_campaignid=16717990508&gbraid=0AAAAADPpU9CzR10TR2w6jARlxmRa5Smpd&gclid=CjwKCAiA3L_JBhAlEiwAlcWO50oNUufF0AydfkcFs8BMI3vE_ykbgITEC8UXnnOho363jLm8HpeFFxoCWd8QAvD_BwE",
        "Water & Food"
    ),

    // First Aid & Hygiene
    ChecklistLink(
        "Complete First Aid Kit",
        "https://shopee.ph/product/873451465/24284190994?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrdWR2LzZQdzdldENMdmEyem9IR1NqUzE0R01ydVVPcW83ekhTcVlLSU45QkxMTzJSK1drNGtkQTBMLzFzMTdLWHZKalBjdTRZeS9uSVNMeE9VNFJhVDE&gad_source=1&gad_campaignid=23303743412&gbraid=0AAAAADPpU9DouHnh5S6YM5H-P76igJDoj&gclid=CjwKCAiA3L_JBhAlEiwAlcWO5yyyKraszQrcIFygnULg5GmCOf3CDsfuabBEpQbaFDlMrQUYsMsVXBoC-nkQAvD_BwE",
        "First Aid & Hygiene"
    ),
    ChecklistLink(
        "Travel Toiletries Kit",
        "https://shopee.ph/product/1281821268/50901158356?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkZG1WNlRuYnZwaTZXVmptQkFDY0kyUXR5M0oxMUNSTzcyMjFiSUhIdldHaE5oTkRycSs3b3dJQ0tTenZPZDdKVWVSQXFUMFdLdDdnMktxRlBkZnkwR21iSUhpckJVVjZCeGs5OWZtVXl4cjVPZ09xbzFsU3o3M1hRYUNZREFTUC9BPT0&gad_source=1&gad_campaignid=16717990508&gbraid=0AAAAADPpU9CzR10TR2w6jARlxmRa5Smpd&gclid=CjwKCAiA3L_JBhAlEiwAlcWO5xsBx4TPDgsLDxQDs1qPnRjeMud3PCOGQSFjbNJT4M5gWb23zsagdRoC7_8QAvD_BwE",
        "First Aid & Hygiene"
    ),
    ChecklistLink(
        "Face Masks (N95 or Surgical)",
        "https://shopee.ph/product/583419942/42925083409?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrc1l2bW80SWdDUmZrUmNybG5mU1R1RHk1eGRLTHEwWGRXa29lQU9lY3JqNFlpaHpLdXVBdy95Q2t0bjBqT2g0QjlCNDlGTFpCajg5WWRsK1d0SkpUWGk&gad_source=1&gad_campaignid=16717990508&gbraid=0AAAAADPpU9CzR10TR2w6jARlxmRa5Smpd&gclid=CjwKCAiA3L_JBhAlEiwAlcWO53T9E-U6HneKciPyeTUEUZwO7V7pmgwFFr5DpyoXJDLLCfYJKGPjzhoCwzUQAvD_BwE",
        "First Aid & Hygiene"
    ),

    // Power, Lighting & Communication
    ChecklistLink(
        "Solar Hand Crank AM/FM Radio",
        "https://shopee.ph/product/202554214/4070092632?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkU1psNndicnpENjFrR2ZiZlcxU0ZETDlPcE5OTWNVR1B5SzJCUldLZkdJS2xXWU92WkJpN2x1VW9tMklSNXdxLzdXTXJWVHZwdmo2V3dESm5MbWpTOWZKKzU0M3k2M1JnbG9Vbmdnd3JrMHk&gad_source=1&gad_campaignid=23037812122&gbraid=0AAAAADPpU9CVdzk0CcuIr9K1d5qIHpT2h&gclid=CjwKCAiA3L_JBhAlEiwAlcWO5xBGseoWTeFuiuw__twleIMEQWL0sodn0F7uURd_f_BJaD2zPZzRHBoCXLkQAvD_BwE",
        "Power, Lighting & Communication"
    ),
    ChecklistLink(
        "Heavy Duty Power Bank (20,000mAh+)",
        "https://shopee.ph/product/1017282719/24736268692?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkZG1WNlRuYnZwaTZXVmptQkFDY0kyUklrOWpYQkk2WlRId2ZuTXVSQW43MHNwd1RrRG50TGJTUWcwOFA2MTdLUk9QVytwYWpYSkF4dDBsazk5WnBPaEg1VzZGQ1B5N3FiZnd6Y20xNm5tNVl1VnpoSU94RlE3dnEvb3ltZ0FLZERBPT0&gad_source=1&gad_campaignid=1674903539&gbraid=0AAAAADPpU9DjcaHcNFxS6Tn7vBE6E_KRf&gclid=CjwKCAiA3L_JBhAlEiwAlcWO57_Us2RYO-ZfaTq2c5BaIp2R9SQacLA0e_5Zqd4YpIh8uH6eq2wEuBoCUNcQAvD_BwE",
        "Power, Lighting & Communication"
    ),
    ChecklistLink(
        "Waterproof Headlamp",
        "https://shopee.ph/product/835876961/24292284558?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrdGlhemM1YXkrZDZEVDd4azR2eldLUUJZYVNXUVFSeU1JVHdWVU1QN1ZRSnVRMUFHTFBCSTZBdGkvWU9VRU5oQjh4a2dOT2xFRnZYZTBZZXFIOEIzOWs&gad_source=1&gad_campaignid=23037812122&gbraid=0AAAAADPpU9CVdzk0CcuIr9K1d5qIHpT2h&gclid=CjwKCAiA3L_JBhAlEiwAlcWO5wbxgx70AtrC-l8ITPT4YZVg2WlA_Hf5EqVMHbsrkqyQ4eKEkACQ6xoC6NUQAvD_BwE",
        "Power, Lighting & Communication"
    ),
    ChecklistLink(
        "Emergency Whistle",
        "https://shopee.ph/product/97297528/29975089148?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkU1psNndicnpENjFrR2ZiZlcxU0ZES29qQVhHUGZ6L2U3SU1rcWRTUDczYWhFWkRBQU1sRGdmYUc0dHNIcGxmUXJTZHp5VWF1dS9lODdVb2ZtZlE2UmJ0ZGtEOHUvWUtJcGtrRU03RjU3cEs&gad_source=1&gad_campaignid=23037812122&gbraid=0AAAAADPpU9CVdzk0CcuIr9K1d5qIHpT2h&gclid=CjwKCAiA3L_JBhAlEiwAlcWO52FpqSeHpBVCNJlUh61qxfuR2CuroSvmDdyyf2sKjEgRYyRMI6TlQBoC1aQQAvD_BwE",
        "Power, Lighting & Communication"
    ),
    ChecklistLink(
        "Spare Batteries",
        "https://shopee.ph/product/858896503/25586010442?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrdEREekRSV1hTQVdtU0VkMGt3cWpDbERIRmxqMGl6UkpzN2hBTzNhTmxQakc0eHBBSjRoS0N1dDEzczRLNXNQSy9yQTlFU1J1bU5KVnQwbHV6b29kUWs&gad_source=1&gad_campaignid=20825349498&gbraid=0AAAAADPpU9AW1HdM-ryc7MQ4g971c0LMc&gclid=CjwKCAiA3L_JBhAlEiwAlcWO50UBrIue9_M4BxyCKpBzC2NScjD0U4NT8oK-AbaSpn9jg1U50XSOvxoCGbUQAvD_BwE",
        "Power, Lighting & Communication"
    ),

    // Shelter & Gear
    ChecklistLink(
        "Heavy Duty Raincoat / Poncho",
        "https://shopee.ph/product/525956336/26538605471?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkVlBXTnFLbGtLY21IOVhMT0xMVGhrc1lwY3BZVDdXM1BWOW5aOEg5Y0liQXRCNmV4M1d2L1hyVzZhN0EydjkxOXhiM0l4VE5FNnllK0s5WnZWQm5Gc3dRY2I4dStlbFUyY0p0Q1F5YXd1Ung&gad_source=1&gad_campaignid=23037812122&gbraid=0AAAAADPpU9CVdzk0CcuIr9K1d5qIHpT2h&gclid=CjwKCAiA3L_JBhAlEiwAlcWO55dplOVT16uRgJO72wWu1Tpd9vFD8JJqf0eltWA4l7h074BwJo1aTRoC1xoQAvD_BwE",
        "Shelter & Gear"
    ),
    ChecklistLink(
        "Emergency Thermal Blanket",
        "https://shopee.ph/product/1209340734/49050234941?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkZG1WNlRuYnZwaTZXVmptQkFDY0kyUkVQMGVxSGYyUVQ5NE9hb1lEZmlaTWNkNGhKOTExV0hGWjhxOHNsQlVtYnV1ZXdrVXgrR1lOZjF2WkVURjFpSlAyVlRCTWVzL3htdFZJTlM4SWNUQVVXK0tMaFNYcUxmT0lWMk5RMnd4N2pnPT0&gad_source=1&gad_campaignid=23037812122&gbraid=0AAAAADPpU9CVdzk0CcuIr9K1d5qIHpT2h&gclid=CjwKCAiA3L_JBhAlEiwAlcWO54V31j2YdG1enWf50lXFznZ9FVUIGnWpH66UP0e486Dhj5wcUCc6ihoCzrEQAvD_BwE",
        "Shelter & Gear"
    ),

    // Pre-Assembled Kits
    ChecklistLink(
        "Complete Emergency Go Bag Set",
        "https://shopee.ph/product/1575929695/29838347103?gads_t_sig=VTJGc2RHVmtYMTlxTFVSVVRrdENkZG1WNlRuYnZwaTZXVmptQkFDY0kyVFV2aUdoNk9wU2swQmQyUVNjdFpXVEVmS05SUDdYRzBmejdJbWNKenhBdm95ZHQ4TjNRQXh1RFF1bHl4R3p4bGY3dlBuUXZzRXNRV3FoSzlJZ2E5MEZ6RXVnbWgzNkdZNWdmSW9ONmp6YWRnPT0&gad_source=1&gad_campaignid=22776341949&gbraid=0AAAAADPpU9C2S4DfYTIHuJOQqJ90bKd7Z&gclid=CjwKCAiA3L_JBhAlEiwAlcWO5yYsF6C3LBvD7GWksx7iOZ8AgUpGTTIt5noPqxs19XgsEybPPqfKqBoClscQAvD_BwE",
        "Pre-Assembled Kits"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckListScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Seed Realm once by label
    LaunchedEffect(Unit) {
        val existing = ChecklistDao.all().map { it.label }.toSet()
        CHECK_ITEMS.forEach { link ->
            if (link.label !in existing) {
                ChecklistDao.add(link.label)
            }
        }
    }

    // Load items
    var items by remember { mutableStateOf(List(0) { ChecklistItem() }) }
    fun refresh() {
        items = ChecklistDao.all()
    }
    LaunchedEffect(Unit) { refresh() }

    // Group by category for display
    val byCategory: Map<String, List<ChecklistLink>> = CHECK_ITEMS.groupBy { it.category }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GO BAG",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            byCategory.forEach { (category, links) ->
                item {
                    // Category card with rounded corners
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE3EDF7))
                            .padding(24.dp)
                    ) {
                        // Category header
                        Text(
                            text = category,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Items in this category
                        links.forEach { link ->
                            val bound = items.find { it.label == link.label }
                            val checked = bound?.isDone == true

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        bound?.let {
                                            scope.launch {
                                                ChecklistDao.toggle(it.id, isChecked)
                                                refresh()
                                            }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF2196F3),
                                        uncheckedColor = Color.Gray
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                val annotated = buildAnnotatedString {
                                    pushStringAnnotation(tag = "URL", annotation = link.url)
                                    pushStyle(
                                        SpanStyle(
                                            color = Color.Black,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    append(link.label)
                                    pop()
                                    pop()
                                }

                                ClickableText(
                                    text = annotated,
                                    onClick = { offset ->
                                        annotated.getStringAnnotations("URL", offset, offset)
                                            .firstOrNull()
                                            ?.let { ann ->
                                                val i = Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                                                ctx.startActivity(i)
                                            }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom padding
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}