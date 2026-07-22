import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchMyGroups, createGroup } from "../api/groupApi";
import {
  Box, Typography, Card, CardContent, TextField, Button, Avatar, Alert, Stack,
} from "@mui/material";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import AddCircleRoundedIcon from "@mui/icons-material/AddCircleRounded";

export default function Groups() {
  const [groups, setGroups] = useState([]);
  const [form, setForm] = useState({ name: "", description: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const load = () => fetchMyGroups().then(setGroups).catch(() => setError("Could not load groups"));

  useEffect(() => { load(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      await createGroup(form);
      setForm({ name: "", description: "" });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create group");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>My Groups</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ mb: 3, borderRadius: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleCreate} sx={{ display: "flex", gap: 1.5, flexWrap: "wrap", alignItems: "center" }}>
            <TextField
              label="Group name" value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required sx={{ flex: "1 1 200px" }}
            />
            <TextField
              label="Description (optional)" value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              sx={{ flex: "2 1 260px" }}
            />
            <Button type="submit" variant="contained" disabled={loading} startIcon={<AddCircleRoundedIcon />}>
              Create Group
            </Button>
          </Box>
        </CardContent>
      </Card>

      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" }, gap: 2 }}>
        {groups.map((g) => (
            <Card
              component={Link}
              to={`/groups/${g.id}`}
              key={g.id}
              sx={{
                borderRadius: 3, textDecoration: "none", display: "block",
                transition: "transform 0.18s ease, box-shadow 0.18s ease",
                "&:hover": { transform: "translateY(-4px)", boxShadow: "0 12px 28px rgba(27,36,48,0.12)" },
              }}
            >
              <CardContent>
                <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
                  <Avatar sx={{ bgcolor: "var(--color-brass)" }}>
                    <GroupsRoundedIcon />
                  </Avatar>
                  <Box>
                    <Typography sx={{ fontFamily: "'Fraunces', serif", fontWeight: 600, fontSize: "1.1rem", color: "var(--color-ink)" }}>
                      {g.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {g.members.length} member{g.members.length !== 1 ? "s" : ""}
                    </Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
        ))}
      </Box>
    </Box>
  );
}